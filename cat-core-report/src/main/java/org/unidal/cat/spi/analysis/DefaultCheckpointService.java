package org.unidal.cat.spi.analysis;

import static org.unidal.cat.spi.ReportPeriod.DAY;
import static org.unidal.cat.spi.ReportPeriod.HOUR;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.report.ReportConfiguration;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportManager;
import org.unidal.cat.spi.report.ReportManagerManager;
import org.unidal.cat.spi.report.internals.ReportDelegateManager;
import org.unidal.cat.spi.report.storage.FileReportStorage;
import org.unidal.cat.spi.report.storage.MysqlReportStorage;
import org.unidal.cat.spi.report.storage.ReportStorage;
import org.unidal.cat.spi.report.task.ReportTaskService;
import org.unidal.helper.Inets;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = CheckpointService.class)
public class DefaultCheckpointService implements CheckpointService {
   @Inject
   private ReportManagerManager m_reportManager;

   @Inject
   private ReportDelegateManager m_delegateManager;

   @Inject
   private ReportConfiguration m_config;

   @Inject(MysqlReportStorage.ID)
   private ReportStorage<Report> m_mysqlStorage;

   @Inject(FileReportStorage.ID)
   private ReportStorage<Report> m_fileStorage;

   @Inject
   private ReportTaskService m_taskService;

   @Override
   public void doCheckpoint(String name, int hour) throws Exception {
      ReportManager<Report> manager = m_reportManager.getReportManager(name);
      ReportDelegate<Report> delegate = m_delegateManager.getDelegate(name);
      List<Map<String, Report>> list = manager.getLocalReports(hour);

      for (Map<String, Report> map : list) {
         int index = 0;

         for (Report report : map.values()) {
            if (m_config.isLocalMode()) {
               m_fileStorage.store(delegate, HOUR, report, index);
            } else {
               m_mysqlStorage.store(delegate, HOUR, report, index);
            }

            index++;
         }
      }
      
      manager.removeLocalReports(hour - 1);

      Date date = new Date(TimeUnit.HOURS.toMillis(hour));
      Date startTime = DAY.getStartTime(date);
      String ip = Inets.IP4.getLocalHostAddress();

      m_taskService.add(ip, DAY, startTime, name, DAY.getReduceTime(startTime));
   }
}

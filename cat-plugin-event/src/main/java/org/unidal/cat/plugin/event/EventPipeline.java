package org.unidal.cat.plugin.event;

import com.dianping.cat.Constants;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.ReportManagerManager;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.analysis.pipeline.AbstractPipeline;
import org.unidal.cat.spi.analysis.pipeline.Pipeline;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.internals.ReportDelegateManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Named(type = Pipeline.class, value = EventConstants.NAME, instantiationStrategy = Named.PER_LOOKUP)
public class EventPipeline extends AbstractPipeline {
   @Inject
   private ReportManagerManager m_rmm;

   @Inject
   private ReportDelegateManager m_rdg;

   @Override
   protected void afterCheckpoint() {

   }

   @Override
   protected void beforeCheckpoint() throws IOException {
      ReportManager<Report> manager = m_rmm.getReportManager(getName());

      ReportDelegate<Report> delegate = m_rdg.getDelegate(getName());

      List<Map<String, Report>> reportMapList = manager.getLocalReports(ReportPeriod.HOUR, getHour());

      if (reportMapList.size() > 0) {
         List<Report> reportList = new ArrayList<Report>();

         for (Map<String, Report> map : reportMapList) {
            reportList.addAll(map.values());
         }

         Report allReport = delegate.makeAll(ReportPeriod.HOUR, reportList);

         Map<String, Report> map = reportMapList.get(0);

         map.put(Constants.ALL, allReport);
      }
   }
}

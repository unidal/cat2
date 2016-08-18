package org.unidal.cat.spi.report.internals;

import static org.unidal.cat.spi.ReportPeriod.DAY;
import static org.unidal.cat.spi.ReportPeriod.HOUR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.remote.DefaultRemoteContext;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.cat.spi.report.ReportFilterManager;
import org.unidal.cat.spi.report.provider.ReportProvider;
import org.unidal.cat.spi.report.storage.FileReportStorage;
import org.unidal.cat.spi.report.storage.ReportStorage;
import org.unidal.cat.spi.report.task.ReportTaskService;
import org.unidal.helper.Inets;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.RoleHintEnabled;

public abstract class AbstractReportManager<T extends Report> implements ReportManager<T>, RoleHintEnabled {
   @Inject
   private ReportProvider<T> m_provider;

   @Inject
   private ReportStorage<T> m_mysqlStorage;

   @Inject(FileReportStorage.ID)
   private ReportStorage<T> m_fileStorage;

   @Inject
   private ReportDelegateManager m_delegateManager;

   @Inject
   private ReportFilterManager m_filterManager;

   @Inject
   private ReportTaskService m_taskService;

   private String m_reportName;

   private ConcurrentMap<Long, ConcurrentMap<String, T>> m_reports = new ConcurrentHashMap<Long, ConcurrentMap<String, T>>();

   @Override
   public void doCheckpoint(int hour, int index) throws Exception {
      Date startTime = new Date(TimeUnit.HOURS.toMillis(hour));
      ConcurrentMap<String, T> map = m_reports.get(startTime.getTime() + index);

      removeReport(hour - 1, index);

      if (map != null && map.size() > 0) {
         List<T> reports = new ArrayList<T>(map.values());

         for (T report : reports) {
            m_fileStorage.store(getDelegate(), HOUR, report, index);
            m_mysqlStorage.store(getDelegate(), HOUR, report, index);
         }

         // 1 AM tommorrow morning for daily report
         Date reportStartTime = ReportPeriod.DAY.getStartTime(startTime);
         String ip = Inets.IP4.getLocalHostAddress();

         m_taskService.add(ip, DAY, reportStartTime, m_reportName, DAY.getReduceTime(reportStartTime));
      }
   }

   @Override
   public void doInitLoad(int hour, int index) throws IOException {
      Date startTime = new Date(TimeUnit.HOURS.toMillis(hour));
      long key = startTime.getTime() + index;
      ConcurrentHashMap<String, T> map = new ConcurrentHashMap<String, T>();
      List<T> reports = m_fileStorage.loadAll(getDelegate(), HOUR, startTime, null);

      for (T report : reports) {
         map.put(report.getDomain(), report);
      }

      m_reports.putIfAbsent(key, map);
   }

   @Override
   public void enableRoleHint(String roleHint) {
      m_reportName = roleHint;
   }

   protected ReportDelegate<T> getDelegate() {
      return m_delegateManager.getDelegate(m_reportName);
   }

   @Override
   public T getLocalReport(String domain, int hour, int index, boolean createIfNotExist) {
      Date startTime = new Date(TimeUnit.HOURS.toMillis(hour));
      long key = startTime.getTime() + index;
      ConcurrentMap<String, T> map = m_reports.get(key);

      if (map == null) {
         synchronized (m_reports) {
            map = m_reports.get(key);

            if (map == null) {
               map = new ConcurrentHashMap<String, T>();

               m_reports.put(key, map);
            }
         }
      }

      T report = map.get(domain);

      if (report == null && createIfNotExist) {
         report = getDelegate().createLocal(HOUR, domain, startTime);

         T r = map.putIfAbsent(domain, report);

         if (r != null) {
            report = r;
         }
      }

      return report;
   }

   public List<T> getLocalReports(ReportPeriod period, Date startTime, String domain, Map<String, String> properties)
         throws IOException {
      if (period == HOUR) {
         int hour = (int) TimeUnit.MILLISECONDS.toHours(startTime.getTime());
         int count = getThreadsCount();
         List<T> reports = new ArrayList<T>(count);

         for (int i = 0; i < count; i++) {
            T report = getLocalReport(domain, hour, i, false);

            if (report != null) {
               reports.add(report);
            }
         }

         if (reports.size() > 0) {
            return reports;
         } else {
            return m_fileStorage.loadAll(getDelegate(), period, startTime, domain);
         }
      } else {
         return m_fileStorage.loadAll(getDelegate(), period, startTime, domain);
      }
   }

   @Override
   public List<Map<String, T>> getLocalReports(ReportPeriod period, int hour) throws IOException {
      List<Map<String, T>> reports = new ArrayList<Map<String, T>>();
      int count = getThreadsCount();

      for (int i = 0; i < count; i++) {
         Map<String, T> map = getLocalReports(period, hour, i);

         if (map != null && map.size() > 0) {
            reports.add(map);
         }
      }

      return reports;
   }

   private Map<String, T> getLocalReports(ReportPeriod period, int hour, int index) throws IOException {
      Date startTime = new Date(TimeUnit.HOURS.toMillis(hour));
      long key = startTime.getTime() + index;

      return m_reports.get(key);
   }

   @Override
   public T getReport(ReportPeriod period, Date startTime, String domain, String filterId, String... keyValuePairs)
         throws IOException {
      ReportDelegate<T> delegate = getDelegate();
      ReportFilter<? extends Report> filter = m_filterManager.getFilter(delegate.getName(), filterId);
      RemoteContext ctx = new DefaultRemoteContext(delegate.getName(), domain, startTime, period, filter);
      int len = keyValuePairs.length;

      if (len % 2 == 0) {
         for (int i = 0; i < len; i += 2) {
            String property = keyValuePairs[i];
            String value = keyValuePairs[i + 1];

            ctx.setProperty(property, value);
         }
      } else {
         throw new IllegalArgumentException("Parameter(keyValuePairs) is not paired!");
      }

      try {
         T report = m_provider.getReport(ctx, delegate);

         return report;
      } finally {
         ctx.destroy();
      }
   }

   public abstract int getThreadsCount();

   @Override
   public void removeReport(int hour, int index) {
      Date startTime = new Date(TimeUnit.HOURS.toMillis(hour));
      long key = startTime.getTime() + index;

      m_reports.remove(key);
   }
}

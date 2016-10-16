package org.unidal.cat.spi.report.internals;

import static org.unidal.cat.spi.ReportPeriod.HOUR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.unidal.cat.core.report.remote.DefaultRemoteReportContext;
import org.unidal.cat.core.report.remote.RemoteReportContext;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.cat.spi.report.ReportFilterManager;
import org.unidal.cat.spi.report.ReportManager;
import org.unidal.cat.spi.report.provider.ReportProvider;
import org.unidal.cat.spi.report.storage.FileReportStorage;
import org.unidal.cat.spi.report.storage.ReportStorage;
import org.unidal.cat.spi.report.task.ReportTaskService;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.RoleHintEnabled;

public abstract class AbstractReportManager<T extends Report> implements ReportManager<T>, RoleHintEnabled {
   @Inject
   private ReportProvider<T> m_provider;

   @Inject(FileReportStorage.ID)
   private ReportStorage<T> m_fileStorage;

   @Inject
   private ReportDelegateManager m_delegateManager;

   @Inject
   private ReportFilterManager m_filterManager;

   @Inject
   private ReportTaskService m_taskService;

   private String m_reportName;

   private ConcurrentMap<Integer, List<ConcurrentMap<String, T>>> m_map = new ConcurrentHashMap<Integer, List<ConcurrentMap<String, T>>>();

   @Override
   public void enableRoleHint(String roleHint) {
      m_reportName = roleHint;
   }

   protected ReportDelegate<T> getDelegate() {
      return m_delegateManager.getDelegate(m_reportName);
   }

   @Override
   public T getLocalReport(String domain, int hour, int index, boolean createIfNotExist) {
      ConcurrentMap<String, T> map = getLocalReports(hour, index, createIfNotExist);
      T report = (map == null ? null : map.get(domain));

      if (report == null && createIfNotExist) {
         Date startTime = new Date(TimeUnit.HOURS.toMillis(hour));
         T r;

         report = getDelegate().createLocal(HOUR, domain, startTime);

         if ((r = map.putIfAbsent(domain, report)) != null) {
            report = r;
         }
      }

      return report;
   }

   @Override
   @SuppressWarnings("unchecked")
   public List<Map<String, T>> getLocalReports(int hour) {
      return (List<Map<String, T>>) (List<? extends Map<String, T>>) m_map.get(hour);
   }

   private ConcurrentMap<String, T> getLocalReports(int hour, int index, boolean createIfNotExists) {
      List<ConcurrentMap<String, T>> list = m_map.get(hour);

      if (list == null && createIfNotExists) {
         List<ConcurrentMap<String, T>> l;

         list = new ArrayList<ConcurrentMap<String, T>>();

         if ((l = m_map.putIfAbsent(hour, list)) != null) {
            list = l;
         }
      }

      if (createIfNotExists) {
         for (int i = list.size(); i < index + 1; i++) {
            list.add(new ConcurrentHashMap<String, T>());
         }
      }

      if (list != null && index >= 0 && index < list.size()) {
         return list.get(index);
      } else {
         return null;
      }
   }

   @Override
   public T getReport(ReportPeriod period, Date startTime, String domain, String filterId, String... keyValuePairs)
         throws IOException {
      ReportDelegate<T> delegate = getDelegate();
      ReportFilter<? extends Report> filter = m_filterManager.getFilter(delegate.getName(), filterId);
      RemoteReportContext ctx = new DefaultRemoteReportContext(delegate.getName(), domain, startTime, period, filter);
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

   public List<T> getReports(ReportPeriod period, Date startTime, String domain, Map<String, String> properties)
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
            // fall back to local file storage
            return m_fileStorage.loadAll(getDelegate(), period, startTime, domain);
         }
      } else {
         return m_fileStorage.loadAll(getDelegate(), period, startTime, domain);
      }
   }

   public int getThreadsCount() {
      return 1;
   }

   @Override
   public void loadLocalReports(int hour, int index) throws IOException {
      Date startTime = new Date(TimeUnit.HOURS.toMillis(hour));
      List<T> reports = m_fileStorage.loadAll(getDelegate(), HOUR, startTime, null);
      ConcurrentMap<String, T> map = getLocalReports(hour, index, true);

      for (T report : reports) {
         map.put(report.getDomain(), report);
      }
   }

   @Override
   public void removeLocalReports(int hour) {
      m_map.remove(hour);
   }
}

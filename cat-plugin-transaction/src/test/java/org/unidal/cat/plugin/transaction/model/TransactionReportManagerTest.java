package org.unidal.cat.plugin.transaction.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.ReportManagerManager;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.internals.ReportDelegateManager;
import org.unidal.cat.spi.report.storage.ReportStorage;
import org.unidal.lookup.ComponentTestCase;

@Ignore
public class TransactionReportManagerTest extends ComponentTestCase {
   @Before
   public void before() throws Exception {
      defineComponent(ReportStorage.class, MockReportStorage.class);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testCheckpoint() throws Exception {
      ReportManagerManager rmm = lookup(ReportManagerManager.class);
      ReportDelegateManager delegateManager = lookup(ReportDelegateManager.class);
      ReportStorage<TransactionReport> storage = lookup(ReportStorage.class);

      ReportDelegate<TransactionReport> delegate = delegateManager.getDelegate(TransactionConstants.NAME);
      ReportManager<TransactionReport> rm = rmm.getReportManager(TransactionConstants.NAME);
      Date startTime = ReportPeriod.HOUR.getStartTime(new Date(1452313569760L));
      int hour = (int) TimeUnit.MILLISECONDS.toHours(startTime.getTime());

      for (int i = 0; i < 2; i++) {
         TransactionReport r1 = rm.getLocalReport("test1", hour, i, true);
         TransactionReport r2 = rm.getLocalReport("test2", hour, i, true);

         r1.addIp("ip" + i);
         r2.addIp("IP" + i);

         rm.doCheckpoint(hour, i);
      }

      List<TransactionReport> reports = storage.loadAll(delegate, ReportPeriod.HOUR, startTime, "test1");

      Assert.assertEquals(2, reports.size());

      // TODO check report task
   }

   public static class MockReportStorage implements ReportStorage<TransactionReport> {
      private Map<String, List<TransactionReport>> m_map = new HashMap<String, List<TransactionReport>>();

      @Override
      public List<TransactionReport> loadAll(ReportDelegate<TransactionReport> delegate, ReportPeriod period,
            Date startTime, String domain) throws IOException {
         String key = period.getName() + "-" + domain + "-" + period.format(startTime);

         return m_map.get(key);
      }

      @Override
      public List<TransactionReport> loadAllByDateRange(ReportDelegate<TransactionReport> delegate,
            ReportPeriod period, Date startTime, Date endTime, String domain) throws IOException {
         throw new UnsupportedOperationException("Not implemented yet!");
      }

      @Override
      public void store(ReportDelegate<TransactionReport> delegate, ReportPeriod period, TransactionReport report,
            int index) throws IOException {
         String key = period.getName() + "-" + report.getDomain() + "-" + period.format(report.getStartTime());
         List<TransactionReport> reports = m_map.get(key);

         if (reports == null) {
            reports = new ArrayList<TransactionReport>();
            m_map.put(key, reports);
         }

         reports.add(report);
      }
   }
}

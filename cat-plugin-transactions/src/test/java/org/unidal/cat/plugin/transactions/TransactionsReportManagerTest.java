package org.unidal.cat.plugin.transactions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.core.config.DomainOrgConfigService;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.TransactionReportManager;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.internals.ReportDelegateManager;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

public class TransactionsReportManagerTest extends ComponentTestCase {
   @Test
   @SuppressWarnings("unchecked")
   public void test() throws Exception {
      defineComponent(DomainOrgConfigService.class, MockDomainOrgConfigService.class);
      defineComponent(ReportManager.class, TransactionConstants.NAME, MockTransactionReportManager.class) //
            .req(ReportDelegateManager.class);

      ReportManager<TransactionsReport> rm = lookup(ReportManager.class, TransactionsConstants.NAME);
      List<TransactionsReport> reports = rm.getLocalReports(ReportPeriod.HOUR, new Date(), null);
      TransactionsReport actual = reports.get(0);
      TransactionsReport expected = loadReport("transactions.xml");

      actual.addBu("Hotel");
      actual.addBu("Flight");

      Assert.assertEquals(expected.toString(), actual.toString());
   }

   @SuppressWarnings("unchecked")
   private TransactionsReport loadReport(String resource) throws Exception {
      ReportDelegate<TransactionsReport> delegate = lookup(ReportDelegate.class, TransactionsConstants.NAME);
      InputStream in = getClass().getResourceAsStream(resource);

      if (in == null) {
         throw new IllegalStateException(String.format("Resource(%s) is not found!", resource));
      }

      String xml = Files.forIO().readFrom(in, "utf-8");
      TransactionsReport report = delegate.parseXml(xml);

      return report;
   }

   public static class MockTransactionReportManager extends TransactionReportManager {
      @Override
      @SuppressWarnings("unchecked")
      public List<Map<String, TransactionReport>> getLocalReports(ReportPeriod period, int hour) throws IOException {
         String resource = "transaction.xml";
         InputStream in = getClass().getResourceAsStream(resource);

         if (in == null) {
            throw new IllegalStateException(String.format("Resource(%s) is not found!", resource));
         }

         String xml = Files.forIO().readFrom(in, "utf-8");
         TransactionReport report = getDelegate().parseXml(xml);
         Map<String, TransactionReport> map = new HashMap<String, TransactionReport>();

         map.put(report.getDomain(), report);

         return Arrays.asList(map);
      }
   }

   public static class MockDomainOrgConfigService implements DomainOrgConfigService {
      @Override
      public String findDepartment(String domain) {
         return "Framework";
      }
   }
}

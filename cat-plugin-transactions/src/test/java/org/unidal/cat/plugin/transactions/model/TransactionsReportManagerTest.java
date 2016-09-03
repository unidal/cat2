package org.unidal.cat.plugin.transactions.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.junit.Test;
import org.unidal.cat.core.config.domain.org.entity.DomainOrgConfigModel;
import org.unidal.cat.core.config.service.DomainOrgConfigService;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.model.TransactionReportManager;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transactions.TransactionsConfigService;
import org.unidal.cat.plugin.transactions.TransactionsConstants;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportManager;
import org.unidal.cat.spi.report.internals.ReportDelegateManager;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

public class TransactionsReportManagerTest extends ComponentTestCase {
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

   @Test
   @SuppressWarnings("unchecked")
   public void test() throws Exception {
      defineComponent(DomainOrgConfigService.class, MockDomainOrgConfigService.class);
      defineComponent(TransactionsConfigService.class, MockTransactionsConfigService.class);

      defineComponent(ReportManager.class, TransactionConstants.NAME, MockTransactionReportManager.class) //
            .req(ReportDelegateManager.class);

      ReportManager<TransactionsReport> rm = lookup(ReportManager.class, TransactionsConstants.NAME);
      Map<String, String> properties = Collections.emptyMap();
      List<TransactionsReport> reports = rm.getReports(ReportPeriod.HOUR, new Date(), null, properties);
      TransactionsReport actual = reports.get(0);
      TransactionsReport expected = loadReport("transactions.xml");

      Assert.assertEquals(expected.toString(), actual.toString());
   }

   @SuppressWarnings("serial")
   public static class MockDomainOrgConfigService implements DomainOrgConfigService {
      private Map<String, String> m_map = new HashMap<String, String>() {
         {
            put("cat", "Framework");
            put("hotel", "Business");
            put("flight", "Business");
         }
      };

      @Override
      public String findDepartment(String domain) {
         return m_map.get(domain);
      }

      @Override
      public DomainOrgConfigModel getConfig() {
         return null;
      }

      @Override
      public boolean isIn(String bu, String domain) {
         return true;
      }
   }

   public static class MockTransactionsConfigService extends TransactionsConfigService {
      @Override
      public boolean isEligible(String type) {
         return "URL".equals(type) || "SQL".equals(type);
      }

      @Override
      public boolean isEligible(String type, String name) {
         if ("SQL".equals(type)) {
            return name.startsWith("hostinfo.");
         }
         return true;
      }

      @Override
      public void initialize() throws InitializationException {
      }
   }

   public static class MockTransactionReportManager extends TransactionReportManager {
      @Override
      @SuppressWarnings("unchecked")
      public List<Map<String, TransactionReport>> getLocalReports(int hour) {
         Map<String, TransactionReport> map = new HashMap<String, TransactionReport>();

         try {
            map.put("cat", loadReport("transaction-cat.xml"));
            map.put("hotel", loadReport("transaction-hotel.xml"));
            map.put("flight", loadReport("transaction-flight.xml"));
         } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
         }

         return Arrays.asList(map);
      }

      private TransactionReport loadReport(String resource) throws IOException {
         InputStream in = getClass().getResourceAsStream(resource);

         if (in == null) {
            throw new IllegalStateException(String.format("Resource(%s) is not found!", resource));
         }

         String xml = Files.forIO().readFrom(in, "utf-8");
         TransactionReport report = getDelegate().parseXml(xml);
         return report;
      }
   }
}

package org.unidal.cat.plugin.transactions.filter;

import java.io.InputStream;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.core.config.DomainGroupConfigService;
import org.unidal.cat.plugin.transactions.TransactionsConstants;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.remote.DefaultRemoteContext;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

public class TransactionsReportFilterTest extends ComponentTestCase {
   @Before
   public void before() throws Exception {
      defineComponent(DomainGroupConfigService.class, MockDomanConfigService.class);
   }

   @SuppressWarnings("unchecked")
   private TransactionsReport filter(String filterId, TransactionsReport report, String... args) {
      ReportFilter<TransactionsReport> filter = lookup(ReportFilter.class, TransactionsConstants.NAME + ":" + filterId);
      RemoteContext ctx = new DefaultRemoteContext(TransactionsConstants.NAME, report.getDomain(),
            report.getStartTime(), ReportPeriod.HOUR, filter);

      if (args.length % 2 != 0) {
         throw new IllegalArgumentException("args should be paired!");
      }

      for (int i = 0; i < args.length; i += 2) {
         String property = args[i];
         String value = args[i + 1];

         ctx.setProperty(property, value);
      }

      TransactionsReport screened = filter.screen(ctx, report);

      filter.tailor(ctx, screened);
      return screened;
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

   @Test
   public void testType() throws Exception {
      TransactionsReport source = loadReport("source.xml");
      TransactionsReport expected = loadReport("type.xml");
      TransactionsReport filtered = filter(TransactionsTypeFilter.ID, source);

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testTypeGroup() throws Exception {
      TransactionsReport source = loadReport("source.xml");
      TransactionsReport expected = loadReport("type-graph.xml");
      TransactionsReport filtered = filter(TransactionsTypeGraphFilter.ID, source, //
            "type", "URL");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }
   
   @Test
   public void testNameGroup() throws Exception {
      TransactionsReport source = loadReport("source.xml");
      TransactionsReport expected = loadReport("name-graph.xml");
      TransactionsReport filtered = filter(TransactionsNameGraphFilter.ID, source, //
            "type", "URL", "name", "/cat/r/t");
      
      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testName() throws Exception {
      TransactionsReport source = loadReport("source.xml");
      TransactionsReport expected = loadReport("name.xml");
      TransactionsReport filtered = filter(TransactionsNameFilter.ID, source, //
            "type", "URL");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   public static class MockDomanConfigService implements DomainGroupConfigService {
      @Override
      public Set<String> getGroups(String domain, Set<String> ips) {
         throw new UnsupportedOperationException("Not used!");
      }

      @Override
      public boolean isInGroup(String domain, String group, String ip) {
         return "192.168.31.158".equals(ip) || "192.168.31.159".equals(ip);
      }
   }
}

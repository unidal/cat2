package org.unidal.cat.plugin.transaction;

import java.io.InputStream;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.core.config.DomainConfigService;
import org.unidal.cat.plugin.transaction.filter.TransactionNameFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionNameGraphFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionTypeFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionTypeGraphFilter;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.remote.DefaultRemoteContext;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

public class TransactionReportFilterTest extends ComponentTestCase {
   @Before
   public void before() throws Exception {
      defineComponent(DomainConfigService.class, MockDomanConfigService.class);
   }

   @SuppressWarnings("unchecked")
   private TransactionReport filter(String filterId, TransactionReport report, String... args) {
      ReportFilter<TransactionReport> filter = lookup(ReportFilter.class, TransactionConstants.NAME + ":" + filterId);
      RemoteContext ctx = new DefaultRemoteContext(TransactionConstants.NAME, report.getDomain(),
            report.getStartTime(), ReportPeriod.HOUR, filter);

      if (args.length % 2 != 0) {
         throw new IllegalArgumentException("args should be paired!");
      }

      for (int i = 0; i < args.length; i += 2) {
         String property = args[i];
         String value = args[i + 1];

         ctx.setProperty(property, value);
      }

      TransactionReport screened = filter.screen(ctx, report);

      filter.tailor(ctx, screened);
      return screened;
   }

   @SuppressWarnings("unchecked")
   private TransactionReport loadReport(String resource) throws Exception {
      ReportDelegate<TransactionReport> delegate = lookup(ReportDelegate.class, TransactionConstants.NAME);
      InputStream in = getClass().getResourceAsStream("filter/" + resource);

      if (in == null) {
         throw new IllegalStateException(String.format("Resource(%s) is not found!", "filter/" + resource));
      }

      String xml = Files.forIO().readFrom(in, "utf-8");
      TransactionReport report = delegate.parseXml(xml);

      return report;
   }

   @Test
   public void testName() throws Exception {
      TransactionReport source = loadReport("source.xml");
      TransactionReport expected = loadReport("name.xml");
      TransactionReport filtered = filter(TransactionNameFilter.ID, source, //
            "type", "URL");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testNameGraph() throws Exception {
      TransactionReport source = loadReport("source.xml");
      TransactionReport expected = loadReport("name-graph.xml");
      TransactionReport filtered = filter(TransactionNameGraphFilter.ID, source, //
            "type", "URL", "name", "/cat/r/t");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testNameGraphWithIp() throws Exception {
      TransactionReport source = loadReport("source.xml");
      TransactionReport expected = loadReport("name-graph-ip.xml");
      TransactionReport filtered = filter(TransactionNameGraphFilter.ID, source, //
            "type", "URL", "name", "/cat/r/t", "ip", "192.168.31.158");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }
   
   @Test
   public void testNameGraphWithGroup() throws Exception {
      TransactionReport source = loadReport("source.xml");
      TransactionReport expected = loadReport("name-graph-group.xml");
      TransactionReport filtered = filter(TransactionNameGraphFilter.ID, source, //
            "type", "URL", "name", "/cat/r/t", "group", "mock");
      
      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testNameWithGroup() throws Exception {
      TransactionReport source = loadReport("source.xml");
      TransactionReport expected = loadReport("name-group.xml");
      TransactionReport filtered = filter(TransactionNameFilter.ID, source, //
            "type", "URL", "group", "mock", "ip", "192.168.31.158");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testNameWithIp() throws Exception {
      TransactionReport source = loadReport("source.xml");
      TransactionReport expected = loadReport("name-ip.xml");
      TransactionReport filtered = filter(TransactionNameFilter.ID, source, //
            "type", "URL", "ip", "192.168.31.158");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testType() throws Exception {
      TransactionReport source = loadReport("source.xml");
      TransactionReport expected = loadReport("type.xml");
      TransactionReport filtered = filter(TransactionTypeFilter.ID, source);

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testTypeGraph() throws Exception {
      TransactionReport source = loadReport("source.xml");
      TransactionReport expected = loadReport("type-graph.xml");
      TransactionReport filtered = filter(TransactionTypeGraphFilter.ID, source, //
            "type", "URL");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testTypeGraphWithGroup() throws Exception {
      TransactionReport source = loadReport("source.xml");
      TransactionReport expected = loadReport("type-graph-group.xml");
      TransactionReport filtered = filter(TransactionTypeGraphFilter.ID, source, //
            "type", "URL", "group", "mock");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testTypeGraphWithIp() throws Exception {
      TransactionReport source = loadReport("source.xml");
      TransactionReport expected = loadReport("type-graph-ip.xml");
      TransactionReport filtered = filter(TransactionTypeGraphFilter.ID, source, //
            "type", "URL", "ip", "192.168.31.158");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testTypeWithGroup() throws Exception {
      TransactionReport source = loadReport("source.xml");
      TransactionReport expected = loadReport("type-group.xml");
      TransactionReport filtered = filter(TransactionTypeFilter.ID, source, //
            "group", "mock");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testTypeWithIp() throws Exception {
      TransactionReport source = loadReport("source.xml");
      TransactionReport expected = loadReport("type-ip.xml");
      TransactionReport filtered = filter(TransactionTypeFilter.ID, source, //
            "ip", "192.168.31.158");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   public static class MockDomanConfigService implements DomainConfigService {
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

package org.unidal.cat.plugin.event.filter;

import java.io.InputStream;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.core.config.service.DomainGroupConfigService;
import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.remote.DefaultRemoteContext;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

public class EventReportFilterTest extends ComponentTestCase {
   @Before
   public void before() throws Exception {
      defineComponent(DomainGroupConfigService.class, MockDomanConfigService.class);
   }

   @SuppressWarnings("unchecked")
   private EventReport filter(String filterId, EventReport report, String... args) {
      ReportFilter<EventReport> filter = lookup(ReportFilter.class, EventConstants.NAME + ":" + filterId);
      RemoteContext ctx = new DefaultRemoteContext(EventConstants.NAME, report.getDomain(),
            report.getStartTime(), ReportPeriod.HOUR, filter);

      if (args.length % 2 != 0) {
         throw new IllegalArgumentException("args should be paired!");
      }

      for (int i = 0; i < args.length; i += 2) {
         String property = args[i];
         String value = args[i + 1];

         ctx.setProperty(property, value);
      }

      EventReport screened = filter.screen(ctx, report);

      filter.tailor(ctx, screened);
      return screened;
   }

   @SuppressWarnings("unchecked")
   private EventReport loadReport(String resource) throws Exception {
      ReportDelegate<EventReport> delegate = lookup(ReportDelegate.class, EventConstants.NAME);
      InputStream in = getClass().getResourceAsStream(resource);

      if (in == null) {
         throw new IllegalStateException(String.format("Resource(%s) is not found!", resource));
      }

      String xml = Files.forIO().readFrom(in, "utf-8");
      EventReport report = delegate.parseXml(xml);

      return report;
   }

   @Test
   public void testName() throws Exception {
      EventReport source = loadReport("source.xml");
      EventReport expected = loadReport("name.xml");
      EventReport filtered = filter(EventNameFilter.ID, source, //
            "type", "URL");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testNameGraph() throws Exception {
      EventReport source = loadReport("source.xml");
      EventReport expected = loadReport("name-graph.xml");
      EventReport filtered = filter(EventNameGraphFilter.ID, source, //
            "type", "URL", "name", "/cat/r/t");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testNameGraphWithIp() throws Exception {
      EventReport source = loadReport("source.xml");
      EventReport expected = loadReport("name-graph-ip.xml");
      EventReport filtered = filter(EventNameGraphFilter.ID, source, //
            "type", "URL", "name", "/cat/r/t", "ip", "10.38.0.14");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testNameGraphWithGroup() throws Exception {
      EventReport source = loadReport("source.xml");
      EventReport expected = loadReport("name-graph-group.xml");
      EventReport filtered = filter(EventNameGraphFilter.ID, source, //
            "type", "URL", "name", "/cat/r/t", "group", "mock");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testNameWithGroup() throws Exception {
      EventReport source = loadReport("source.xml");
      EventReport expected = loadReport("name-group.xml");
      EventReport filtered = filter(EventNameFilter.ID, source, //
            "type", "URL", "group", "mock", "ip", "10.38.0.14");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testNameWithIp() throws Exception {
      EventReport source = loadReport("source.xml");
      EventReport expected = loadReport("name-ip.xml");
      EventReport filtered = filter(EventNameFilter.ID, source, //
            "type", "URL", "ip", "10.38.0.14");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testType() throws Exception {
      EventReport source = loadReport("source.xml");
      EventReport expected = loadReport("type.xml");
      EventReport filtered = filter(EventTypeFilter.ID, source);

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testTypeGraph() throws Exception {
      EventReport source = loadReport("source.xml");
      EventReport expected = loadReport("type-graph.xml");
      EventReport filtered = filter(EventTypeGraphFilter.ID, source, //
            "type", "URL");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testTypeGraphWithGroup() throws Exception {
      EventReport source = loadReport("source.xml");
      EventReport expected = loadReport("type-graph-group.xml");
      EventReport filtered = filter(EventTypeGraphFilter.ID, source, //
            "type", "URL", "group", "mock");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testTypeGraphWithIp() throws Exception {
      EventReport source = loadReport("source.xml");
      EventReport expected = loadReport("type-graph-ip.xml");
      EventReport filtered = filter(EventTypeGraphFilter.ID, source, //
            "type", "URL", "ip", "10.38.0.14");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testTypeWithGroup() throws Exception {
      EventReport source = loadReport("source.xml");
      EventReport expected = loadReport("type-group.xml");
      EventReport filtered = filter(EventTypeFilter.ID, source, //
            "group", "mock");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testTypeWithIp() throws Exception {
      EventReport source = loadReport("source.xml");
      EventReport expected = loadReport("type-ip.xml");
      EventReport filtered = filter(EventTypeFilter.ID, source, //
            "ip", "10.38.0.14");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   public static class MockDomanConfigService implements DomainGroupConfigService {
      @Override
      public Set<String> getGroups(String domain, Set<String> ips) {
         throw new UnsupportedOperationException("Not used!");
      }

      @Override
      public boolean isInGroup(String domain, String group, String ip) {
         return "10.38.0.14".equals(ip) || "10.38.0.15".equals(ip);
      }
   }
}

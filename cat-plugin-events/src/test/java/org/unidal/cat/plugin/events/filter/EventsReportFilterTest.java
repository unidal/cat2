package org.unidal.cat.plugin.events.filter;

import java.io.InputStream;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.core.config.DomainGroupConfigService;
import org.unidal.cat.plugin.events.EventsConstants;
import org.unidal.cat.plugin.events.model.entity.EventsReport;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.remote.DefaultRemoteContext;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

public class EventsReportFilterTest extends ComponentTestCase {
   @Before
   public void before() throws Exception {
      defineComponent(DomainGroupConfigService.class, MockDomanConfigService.class);
   }

   @SuppressWarnings("unchecked")
   private EventsReport filter(String filterId, EventsReport report, String... args) {
      ReportFilter<EventsReport> filter = lookup(ReportFilter.class, EventsConstants.NAME + ":" + filterId);
      RemoteContext ctx = new DefaultRemoteContext(EventsConstants.NAME, report.getDomain(),
            report.getStartTime(), ReportPeriod.HOUR, filter);

      if (args.length % 2 != 0) {
         throw new IllegalArgumentException("args should be paired!");
      }

      for (int i = 0; i < args.length; i += 2) {
         String property = args[i];
         String value = args[i + 1];

         ctx.setProperty(property, value);
      }

      EventsReport screened = filter.screen(ctx, report);

      filter.tailor(ctx, screened);
      return screened;
   }

   @SuppressWarnings("unchecked")
   private EventsReport loadReport(String resource) throws Exception {
      ReportDelegate<EventsReport> delegate = lookup(ReportDelegate.class, EventsConstants.NAME);
      InputStream in = getClass().getResourceAsStream(resource);

      if (in == null) {
         throw new IllegalStateException(String.format("Resource(%s) is not found!", resource));
      }

      String xml = Files.forIO().readFrom(in, "utf-8");
      EventsReport report = delegate.parseXml(xml);

      return report;
   }

   @Test
   public void testType() throws Exception {
      EventsReport source = loadReport("source.xml");
      EventsReport expected = loadReport("type.xml");
      EventsReport filtered = filter(EventsTypeFilter.ID, source);

      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testTypeGroup() throws Exception {
      EventsReport source = loadReport("source.xml");
      EventsReport expected = loadReport("type-graph.xml");
      EventsReport filtered = filter(EventsTypeGraphFilter.ID, source, //
            "type", "URL");

      Assert.assertEquals(expected.toString(), filtered.toString());
   }
   
   @Test
   public void testNameGroup() throws Exception {
      EventsReport source = loadReport("source.xml");
      EventsReport expected = loadReport("name-graph.xml");
      EventsReport filtered = filter(EventsNameGraphFilter.ID, source, //
            "type", "URL", "name", "/cat/r/t");
      
      Assert.assertEquals(expected.toString(), filtered.toString());
   }

   @Test
   public void testName() throws Exception {
      EventsReport source = loadReport("source.xml");
      EventsReport expected = loadReport("name.xml");
      EventsReport filtered = filter(EventsNameFilter.ID, source, //
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

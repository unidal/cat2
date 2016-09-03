package org.unidal.cat.plugin.events.model;

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
import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.plugin.event.model.EventReportManager;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.plugin.events.EventsConfigService;
import org.unidal.cat.plugin.events.EventsConstants;
import org.unidal.cat.plugin.events.model.entity.EventsReport;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportManager;
import org.unidal.cat.spi.report.internals.ReportDelegateManager;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

public class EventsReportManagerTest extends ComponentTestCase {
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
   @SuppressWarnings("unchecked")
   public void test() throws Exception {
      defineComponent(DomainOrgConfigService.class, MockDomainOrgConfigService.class);
      defineComponent(EventsConfigService.class, MockEventsConfigService.class);

      defineComponent(ReportManager.class, EventConstants.NAME, MockEventReportManager.class) //
            .req(ReportDelegateManager.class);

      ReportManager<EventsReport> rm = lookup(ReportManager.class, EventsConstants.NAME);
      Map<String, String> properties = Collections.emptyMap();
      List<EventsReport> reports = rm.getReports(ReportPeriod.HOUR, new Date(), null, properties);
      EventsReport actual = reports.get(0);
      EventsReport expected = loadReport("events.xml");

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

   public static class MockEventsConfigService extends EventsConfigService {
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

   public static class MockEventReportManager extends EventReportManager {
      @Override
      @SuppressWarnings("unchecked")
      public List<Map<String, EventReport>> getLocalReports(int hour) {
         Map<String, EventReport> map = new HashMap<String, EventReport>();

         try {
            map.put("cat", loadReport("event-cat.xml"));
            map.put("hotel", loadReport("event-hotel.xml"));
            map.put("flight", loadReport("event-flight.xml"));
         } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
         }

         return Arrays.asList(map);
      }

      private EventReport loadReport(String resource) throws IOException {
         InputStream in = getClass().getResourceAsStream(resource);

         if (in == null) {
            throw new IllegalStateException(String.format("Resource(%s) is not found!", resource));
         }

         String xml = Files.forIO().readFrom(in, "utf-8");
         EventReport report = getDelegate().parseXml(xml);
         return report;
      }
   }
}

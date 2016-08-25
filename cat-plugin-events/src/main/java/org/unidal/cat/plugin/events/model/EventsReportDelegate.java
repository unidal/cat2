package org.unidal.cat.plugin.events.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;

import org.unidal.cat.plugin.events.EventsConstants;
import org.unidal.cat.plugin.events.model.entity.EventsReport;
import org.unidal.cat.plugin.events.model.transform.DefaultNativeBuilder;
import org.unidal.cat.plugin.events.model.transform.DefaultNativeParser;
import org.unidal.cat.plugin.events.model.transform.DefaultSaxParser;
import org.unidal.cat.plugin.events.model.transform.DefaultXmlBuilder;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportDelegate.class, value = EventsConstants.NAME)
public class EventsReportDelegate implements ReportDelegate<EventsReport> {
   @Inject(EventsConstants.NAME)
   private ReportAggregator<EventsReport> m_aggregator;

   @Override
   public EventsReport aggregate(ReportPeriod period, Collection<EventsReport> reports) {
      return m_aggregator.aggregate(period, reports);
   }

   @Override
   public String buildXml(EventsReport report) {
      String xml = new DefaultXmlBuilder().buildXml(report);

      return xml;
   }

   @Override
   public EventsReport createLocal(ReportPeriod period, String domain, Date startTime) {
      return new EventsReport().setPeriod(period).setStartTime(startTime);
   }

   @Override
   public String getName() {
      return EventsConstants.NAME;
   }

   @Override
   public EventsReport parseXml(String xml) {
      try {
         return DefaultSaxParser.parse(xml);
      } catch (Exception e) {
         throw new IllegalArgumentException("Invalid XML! length = " + xml.length(), e);
      }
   }

   @Override
   public EventsReport readStream(InputStream in) {
      EventsReport report = DefaultNativeParser.parse(in);

      if (report.getDomain() == null) {
         return null;
      } else {
         return report;
      }
   }

   @Override
   public void writeStream(OutputStream out, EventsReport report) {
      DefaultNativeBuilder.build(report, out);
   }
}

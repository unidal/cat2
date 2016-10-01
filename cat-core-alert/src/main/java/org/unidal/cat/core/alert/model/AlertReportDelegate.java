package org.unidal.cat.core.alert.model;

import org.unidal.cat.core.alert.AlertConstants;
import org.unidal.cat.core.alert.model.entity.AlertReport;
import org.unidal.cat.core.alert.model.transform.DefaultNativeBuilder;
import org.unidal.cat.core.alert.model.transform.DefaultNativeParser;
import org.unidal.cat.core.alert.model.transform.DefaultSaxParser;
import org.unidal.cat.core.alert.model.transform.DefaultXmlBuilder;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;

@Named(type = ReportDelegate.class, value = AlertConstants.NAME)
public class AlertReportDelegate implements ReportDelegate<AlertReport> {
   @Inject(AlertConstants.NAME)
   private ReportAggregator<AlertReport> m_aggregator;

   @Override
   public AlertReport aggregate(ReportPeriod period, Collection<AlertReport> reports) {
      return m_aggregator.aggregate(period, reports);
   }

   @Override
   public String buildXml(AlertReport report) {
      String xml = new DefaultXmlBuilder().buildXml(report);

      return xml;
   }

   @Override
   public AlertReport createLocal(ReportPeriod period, String domain, Date startTime) {
      return new AlertReport(domain).setPeriod(period).setStartTime(startTime);
   }

   @Override
   public String getName() {
      return AlertConstants.NAME;
   }

   @Override
   public AlertReport parseXml(String xml) {
      try {
         return DefaultSaxParser.parse(xml);
      } catch (Exception e) {
         throw new IllegalArgumentException("Invalid XML! length = " + xml.length(), e);
      }
   }

   @Override
   public AlertReport readStream(InputStream in) {
      AlertReport report = DefaultNativeParser.parse(in);

      if (report.getDomain() == null) {
         return null;
      } else {
         return report;
      }
   }

   @Override
   public void writeStream(OutputStream out, AlertReport report) {
      DefaultNativeBuilder.build(report, out);
   }
}

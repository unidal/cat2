package org.unidal.cat.plugin.transactions;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;

import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.plugin.transactions.model.transform.DefaultNativeBuilder;
import org.unidal.cat.plugin.transactions.model.transform.DefaultNativeParser;
import org.unidal.cat.plugin.transactions.model.transform.DefaultSaxParser;
import org.unidal.cat.plugin.transactions.model.transform.DefaultXmlBuilder;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportDelegate.class, value = TransactionsConstants.NAME)
public class TransactionsReportDelegate implements ReportDelegate<TransactionsReport> {
   // @Inject(TransactionsConstants.NAME)
   private ReportAggregator<TransactionsReport> m_aggregator;

   @Override
   public TransactionsReport aggregate(ReportPeriod period, Collection<TransactionsReport> reports) {
      return m_aggregator.aggregate(period, reports);
   }

   @Override
   public String buildXml(TransactionsReport report) {
      String xml = new DefaultXmlBuilder().buildXml(report);

      return xml;
   }

   @Override
   public TransactionsReport createLocal(ReportPeriod period, String domain, Date startTime) {
      return new TransactionsReport().setPeriod(period).setStartTime(startTime);
   }

   @Override
   public String getName() {
      return TransactionsConstants.NAME;
   }

   @Override
   public TransactionsReport makeAll(ReportPeriod period, Collection<TransactionsReport> reports) {
      return m_aggregator.makeAll(period, reports);
   }

   @Override
   public TransactionsReport parseXml(String xml) {
      try {
         return DefaultSaxParser.parse(xml);
      } catch (Exception e) {
         throw new IllegalArgumentException("Invalid XML! length = " + xml.length(), e);
      }
   }

   @Override
   public TransactionsReport readStream(InputStream in) {
      TransactionsReport report = DefaultNativeParser.parse(in);

      if (report.getDomain() == null) {
         return null;
      } else {
         return report;
      }
   }

   @Override
   public void writeStream(OutputStream out, TransactionsReport report) {
      DefaultNativeBuilder.build(report, out);
   }
}

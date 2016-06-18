package org.unidal.cat.plugin.transaction;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.transaction.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;

@Named(type = ReportDelegate.class, value = TransactionConstants.NAME)
public class TransactionReportDelegate implements ReportDelegate<TransactionReport> {
   @Inject(TransactionConstants.NAME)
   private ReportAggregator<TransactionReport> m_aggregator;

   @Override
   public TransactionReport aggregate(ReportPeriod period, Collection<TransactionReport> reports) {
      return m_aggregator.aggregate(period, reports);
   }

   @Override
   public String buildXml(TransactionReport report) {
      String xml = new DefaultXmlBuilder().buildXml(report);

      return xml;
   }

   @Override
   public TransactionReport createLocal(ReportPeriod period, String domain, Date startTime) {
      return new TransactionReport(domain).setPeriod(period).setStartTime(startTime);
   }

   @Override
   public String getName() {
      return TransactionConstants.NAME;
   }

   @Override
   public TransactionReport makeAll(ReportPeriod period, Collection<TransactionReport> reports) {
      return m_aggregator.makeAll(period, reports);
   }

   @Override
   public TransactionReport parseXml(String xml) {
      try {
         return DefaultSaxParser.parse(xml);
      } catch (Exception e) {
         throw new IllegalArgumentException("Invalid XML! length = " + xml.length(), e);
      }
   }

   @Override
   public TransactionReport readStream(InputStream in) {
      TransactionReport report = DefaultNativeParser.parse(in);

      if (report.getDomain() == null) {
         return null;
      } else {
         return report;
      }
   }

   @Override
   public void writeStream(OutputStream out, TransactionReport report) {
      DefaultNativeBuilder.build(report, out);
   }
}

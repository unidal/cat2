package org.unidal.cat.plugin.transaction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;

import org.unidal.cat.report.ReportPeriod;
import org.unidal.cat.report.spi.ReportAggregator;
import org.unidal.cat.report.spi.ReportDelegate;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.transaction.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder;

@Named(type = ReportDelegate.class, value = TransactionConstants.NAME)
public class TransactionReportDelegate implements ReportDelegate<TransactionReport> {
	@Inject(TransactionConstants.NAME)
	private ReportAggregator<TransactionReport> m_aggregator;

	@Override
	public TransactionReport aggregate(ReportPeriod period, Collection<TransactionReport> reports) {
		return m_aggregator.aggregate(period, reports);
	}

	@Override
	public byte[] buildBinary(TransactionReport report) {
		byte[] data = DefaultNativeBuilder.build(report);

		return data;
	}

	@Override
	public String buildXml(TransactionReport report) {
		String xml = new DefaultXmlBuilder().buildXml(report);

		return xml;
	}

	@Override
	public String getName() {
		return TransactionConstants.NAME;
	}

	@Override
	public TransactionReport parseBinary(byte[] content) {
		return DefaultNativeParser.parse(content);
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

	@Override
	public TransactionReport createLocal(ReportPeriod period, String domain, Date startTime) {
		return new TransactionReport(domain).setPeriod(period).setStartTime(startTime);
	}
}

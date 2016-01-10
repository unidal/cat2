package org.unidal.cat.plugin.event;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;

import org.unidal.cat.report.ReportPeriod;
import org.unidal.cat.report.spi.ReportAggregator;
import org.unidal.cat.report.spi.ReportDelegate;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.event.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.event.model.transform.DefaultXmlBuilder;

@Named(type = ReportDelegate.class, value = EventConstants.ID)
public class EventReportDelegate implements ReportDelegate<EventReport> {
	@Inject(EventConstants.ID)
	private ReportAggregator<EventReport> m_aggregator;

	@Override
	public EventReport aggregate(ReportPeriod period, Collection<EventReport> reports) {
		return m_aggregator.aggregate(period, reports);
	}

	public EventReport getLocalReport(ReportPeriod period, Date startTime, String domain) {
		return null;
	}

	@Override
	public byte[] buildBinary(EventReport report) {
		byte[] data = DefaultNativeBuilder.build(report);

		return data;
	}

	@Override
	public String buildXml(EventReport report) {
		String xml = new DefaultXmlBuilder().buildXml(report);

		return xml;
	}

	@Override
	public String getName() {
		return EventConstants.ID;
	}

	@Override
	public EventReport parseBinary(byte[] content) {
		return DefaultNativeParser.parse(content);
	}

	@Override
	public EventReport parseXml(String xml) {
		try {
			return DefaultSaxParser.parse(xml);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid XML! length = " + xml.length(), e);
		}
	}

	@Override
	public EventReport readStream(InputStream in) {
		return DefaultNativeParser.parse(in);
	}

	@Override
	public void writeStream(OutputStream out, EventReport report) {
		DefaultNativeBuilder.build(report, out);
	}

	@Override
	public EventReport createLocal(ReportPeriod period, String domain, Date startTime) {
		return new EventReport(domain).setPeriod(period).setStartTime(startTime);
	}
}

package org.unidal.cat.plugin.event;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;

import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.event.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.event.model.transform.DefaultXmlBuilder;

@Named(type = ReportDelegate.class, value = EventConstants.NAME)
public class EventReportDelegate implements ReportDelegate<EventReport> {
	@Inject(EventConstants.NAME)
	private ReportAggregator<EventReport> m_aggregator;

	@Override
	public EventReport aggregate(ReportPeriod period, Collection<EventReport> reports) {
		return m_aggregator.aggregate(period, reports);
	}

	@Override
	public EventReport makeAll(ReportPeriod period, Collection<EventReport> reports) {
        return m_aggregator.makeAll(period, reports);
	}

	@Override
	public String buildXml(EventReport report) {
		String xml = new DefaultXmlBuilder().buildXml(report);

		return xml;
	}

	@Override
	public EventReport createLocal(ReportPeriod period, String domain, Date startTime) {
		return new EventReport(domain).setPeriod(period).setStartTime(startTime);
	}

	@Override
	public String getName() {
		return EventConstants.NAME;
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
}

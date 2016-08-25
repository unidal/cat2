package org.unidal.cat.plugin.events.model;

import java.io.InputStream;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.plugin.events.EventsConstants;
import org.unidal.cat.plugin.events.model.EventsReportAggregator;
import org.unidal.cat.plugin.events.model.entity.EventsReport;
import org.unidal.cat.plugin.events.model.transform.DefaultSaxParser;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.lookup.ComponentTestCase;

public class EventsReportAggregatorTest extends ComponentTestCase {
	private EventsReport loadReport(String resource) throws Exception {
		InputStream in = getClass().getResourceAsStream(resource);

		if (in == null) {
			throw new IllegalArgumentException(String.format("Resource(%s) not found!", resource));
		}

		EventsReport report = DefaultSaxParser.parse(in);

		return report;
	}

	@Test
	public void testAggregate() throws Exception {
		EventsReportAggregator aggregator = (EventsReportAggregator) lookup(ReportAggregator.class,
		      EventsConstants.NAME);
		EventsReport p1 = loadReport("events-1.xml");
		EventsReport p2 = loadReport("events-2.xml");
		EventsReport expected = loadReport("events-all.xml");
		EventsReport actual = aggregator.aggregate(ReportPeriod.HOUR, Arrays.asList(p1, p2));

		Assert.assertEquals(expected.toString(), actual.toString());
	}
}

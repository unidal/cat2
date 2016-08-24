package org.unidal.cat.plugin.event.model;

import java.io.InputStream;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.plugin.event.model.EventReportAggregator;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.plugin.event.model.transform.DefaultSaxParser;

public class EventReportAggregatorTest extends ComponentTestCase {
	private EventReport loadReport(String resource) throws Exception {
		InputStream in = getClass().getResourceAsStream(resource);

		if (in == null) {
			throw new IllegalArgumentException(String.format("Resource(%s) not found!", resource));
		}

		EventReport report = DefaultSaxParser.parse(in);

		return report;
	}

	@Test
	public void testAggregate() throws Exception {
		EventReportAggregator aggregator = (EventReportAggregator) lookup(ReportAggregator.class,
		      EventConstants.NAME);
		EventReport p1 = loadReport("event-1.xml");
		EventReport p2 = loadReport("event-2.xml");
		EventReport expected = loadReport("event.xml");
		EventReport actual = aggregator.aggregate(ReportPeriod.HOUR, Arrays.asList(p1, p2));

		Assert.assertEquals(expected.toString(), actual.toString());
	}
}

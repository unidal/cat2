package org.unidal.cat.plugin.event;

import java.io.InputStream;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.plugin.event.EventReportAggregator;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.dal.jdbc.test.JdbcTestCase;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;

public class EventReportAggregatorTest extends JdbcTestCase {
	@Before
	public void before() throws Exception {
		createTables("report");
	}

	@Override
	protected String getDefaultDataSource() {
		return "cat";
	}

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
		EventReport p1 = loadReport("aggregate_p1.xml");
		EventReport p2 = loadReport("aggregate_p2.xml");
		EventReport expected = loadReport("aggregate_all.xml");
		EventReport actual = aggregator.aggregate(ReportPeriod.HOUR, Arrays.asList(p1, p2));

		Assert.assertEquals(expected.toString(), actual.toString());
	}
}

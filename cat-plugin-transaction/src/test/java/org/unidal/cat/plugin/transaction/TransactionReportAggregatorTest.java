package org.unidal.cat.plugin.transaction;

import java.io.InputStream;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.lookup.ComponentTestCase;

import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.model.transform.DefaultSaxParser;

public class TransactionReportAggregatorTest extends ComponentTestCase {
	private TransactionReport loadReport(String resource) throws Exception {
		InputStream in = getClass().getResourceAsStream(resource);

		if (in == null) {
			throw new IllegalArgumentException(String.format("Resource(%s) not found!", resource));
		}

		TransactionReport report = DefaultSaxParser.parse(in);

		return report;
	}

	@Test
	public void testAggregate() throws Exception {
		TransactionReportAggregator aggregator = (TransactionReportAggregator) lookup(ReportAggregator.class,
		      TransactionConstants.NAME);
		TransactionReport p1 = loadReport("aggregate_p1.xml");
		TransactionReport p2 = loadReport("aggregate_p2.xml");
		TransactionReport expected = loadReport("aggregate_all.xml");
		TransactionReport actual = aggregator.aggregate(ReportPeriod.HOUR, Arrays.asList(p1, p2));

		Assert.assertEquals(expected.toString(), actual.toString());
	}
}

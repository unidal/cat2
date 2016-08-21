package org.unidal.cat.plugin.transactions.model;

import java.io.InputStream;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.plugin.transactions.TransactionsConstants;
import org.unidal.cat.plugin.transactions.model.TransactionsReportAggregator;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.plugin.transactions.model.transform.DefaultSaxParser;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.lookup.ComponentTestCase;

public class TransactionsReportAggregatorTest extends ComponentTestCase {
	private TransactionsReport loadReport(String resource) throws Exception {
		InputStream in = getClass().getResourceAsStream(resource);

		if (in == null) {
			throw new IllegalArgumentException(String.format("Resource(%s) not found!", resource));
		}

		TransactionsReport report = DefaultSaxParser.parse(in);

		return report;
	}

	@Test
	public void testAggregate() throws Exception {
		TransactionsReportAggregator aggregator = (TransactionsReportAggregator) lookup(ReportAggregator.class,
		      TransactionsConstants.NAME);
		TransactionsReport p1 = loadReport("transactions-1.xml");
		TransactionsReport p2 = loadReport("transactions-2.xml");
		TransactionsReport expected = loadReport("transactions-all.xml");
		TransactionsReport actual = aggregator.aggregate(ReportPeriod.HOUR, Arrays.asList(p1, p2));

		Assert.assertEquals(expected.toString(), actual.toString());
	}
}

package org.unidal.cat.plugin.transaction.reducer;

import java.io.InputStream;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportReducer;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

public class TransactionReportReducerTest extends ComponentTestCase {
	@SuppressWarnings("unchecked")
	private TransactionReport loadReport(String resource) throws Exception {
		ReportDelegate<TransactionReport> delegate = lookup(ReportDelegate.class, TransactionConstants.NAME);
		InputStream in = getClass().getResourceAsStream(resource);

		if (in == null) {
			throw new IllegalStateException(String.format("Resource(%s) is not found!", resource));
		}

		String xml = Files.forIO().readFrom(in, "utf-8");
		TransactionReport report = delegate.parseXml(xml);

		return report;
	}

	@SuppressWarnings("unchecked")
	private TransactionReport reduce(String id, TransactionReport... reports) {
		ReportReducer<TransactionReport> reducer = lookup(ReportReducer.class, TransactionConstants.NAME + ":" + id);

		return reducer.reduce(Arrays.asList(reports));
	}

	@Test
	public void testDaily() throws Exception {
		TransactionReport expected = loadReport("daily.xml");
		TransactionReport r1 = loadReport("daily-hour-1.xml");
		TransactionReport r2 = loadReport("daily-hour-2.xml");

		String s1 = r1.toString();
		String s2 = r2.toString();

		TransactionReport actual = reduce(TransactionDailyReducer.ID, r1, r2);

		// input reports should NOT be changed
		Assert.assertEquals(s1, r1.toString());
		Assert.assertEquals(s2, r2.toString());
		
		Assert.assertEquals(expected.toString(), actual.toString());
	}

	@Test
	public void testWeekly() throws Exception {
		TransactionReport expected = loadReport("weekly.xml");
		TransactionReport r1 = loadReport("weekly-day-1.xml");
		TransactionReport r2 = loadReport("weekly-day-2.xml");

		String s1 = r1.toString();
		String s2 = r2.toString();

		TransactionReport actual = reduce(TransactionWeeklyReducer.ID, r1, r2);

		// input reports should NOT be changed
		Assert.assertEquals(s1, r1.toString());
		Assert.assertEquals(s2, r2.toString());
		
		Assert.assertEquals(expected.toString(), actual.toString());
	}

	@Test
	public void testMonthly() throws Exception {
		TransactionReport expected = loadReport("monthly.xml");
		TransactionReport r1 = loadReport("monthly-day-1.xml");
		TransactionReport r2 = loadReport("monthly-day-2.xml");

		String s1 = r1.toString();
		String s2 = r2.toString();

		TransactionReport actual = reduce(TransactionMonthlyReducer.ID, r1, r2);

		// input reports should NOT be changed
		Assert.assertEquals(s1, r1.toString());
		Assert.assertEquals(s2, r2.toString());

		Assert.assertEquals(expected.toString(), actual.toString());
	}
}
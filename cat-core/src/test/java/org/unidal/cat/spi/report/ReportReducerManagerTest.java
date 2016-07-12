package org.unidal.cat.spi.report;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.LookupException;

public class ReportReducerManagerTest extends ComponentTestCase {
	@Test(expected = LookupException.class)
	public void testMissing() throws Exception {
		ReportReducerManager manager = lookup(ReportReducerManager.class);

		manager.getReducer("mock", "mock");
	}

	@Test
	public void testNormal() throws Exception {
		defineComponent(ReportReducer.class, "mock:mock", MockReportReducer.class);

		ReportReducerManager manager = lookup(ReportReducerManager.class);
		ReportReducer<Report> reducer = manager.getReducer("mock", "mock");
		ReportReducer<Report> reducer2 = manager.getReducer("mock", "mock");

		Assert.assertEquals("mock", reducer.getReportName());
		Assert.assertEquals("mock", reducer.getId());
		Assert.assertSame(reducer, reducer2);
	}

	public static class MockReportReducer implements ReportReducer<Report> {
		@Override
		public String getId() {
			return "mock";
		}

		@Override
		public String getReportName() {
			return "mock";
		}

		@Override
		public ReportPeriod getPeriod() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Report reduce(List<Report> reports) {
			throw new UnsupportedOperationException();
		}
	}
}

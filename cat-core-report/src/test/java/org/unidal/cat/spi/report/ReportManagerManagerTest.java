package org.unidal.cat.spi.report;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.report.ReportManager;
import org.unidal.cat.spi.report.ReportManagerManager;
import org.unidal.cat.spi.report.internals.AbstractReportManager;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Named;

public class ReportManagerManagerTest extends ComponentTestCase {
	@Test
	public void testConfiguration() throws Exception {
		defineComponent(ReportManager.class, MockReportManager.ID, MockReportManager.class);

		ReportManagerManager rmm = lookup(ReportManagerManager.class);

		Assert.assertTrue(rmm.hasReportManager(MockReportManager.ID));
		Assert.assertSame(MockReportManager.class, rmm.getReportManager(MockReportManager.ID).getClass());

		try {
			rmm.getReportManager("undefined");

			Assert.fail("Should fail, ReportManager(undefined) is not configured yet.");
		} catch (IllegalStateException e) {
			// expected
		}
	}

	@Named(type = ReportManager.class, value = MockReportManager.ID)
	public static class MockReportManager extends AbstractReportManager<Report> {
		public static final String ID = "mock";

		@Override
		public int getThreadsCount() {
			return 1;
		}
	}
}

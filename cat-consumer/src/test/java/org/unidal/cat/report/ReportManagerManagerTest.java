package org.unidal.cat.report;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.transaction.report.TransactionConstants;
import org.unidal.cat.transaction.report.TransactionReportManager;
import org.unidal.lookup.ComponentTestCase;

public class ReportManagerManagerTest extends ComponentTestCase {
	@Test
	public void testConfiguration() {
		ReportManagerManager rmm = lookup(ReportManagerManager.class);

		Assert.assertTrue(rmm.hasReportManager(TransactionConstants.ID));
		Assert.assertSame(TransactionReportManager.class, rmm.getReportManager(TransactionConstants.ID).getClass());

		try {
			rmm.getReportManager("undefined");

			Assert.fail("Should fail, ReportManager(undefined) is not configured yet.");
		} catch (IllegalStateException e) {
			// expected
		}
	}
}

package org.unidal.cat.report;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.TransactionReportManager;
import org.unidal.lookup.ComponentTestCase;

public class ReportManagerManagerTest extends ComponentTestCase {
	@Test
	public void testConfiguration() {
		ReportManagerManager rmm = lookup(ReportManagerManager.class);

		Assert.assertTrue(rmm.hasReportManager(TransactionConstants.NAME));
		Assert.assertSame(TransactionReportManager.class, rmm.getReportManager(TransactionConstants.NAME).getClass());

		try {
			rmm.getReportManager("undefined");

			Assert.fail("Should fail, ReportManager(undefined) is not configured yet.");
		} catch (IllegalStateException e) {
			// expected
		}
	}
}

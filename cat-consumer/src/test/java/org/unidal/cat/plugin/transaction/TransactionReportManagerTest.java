package org.unidal.cat.plugin.transaction;

import java.io.File;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.unidal.cat.spi.ReportConfiguration;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.ReportManagerManager;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.internals.ReportDelegateManager;
import org.unidal.cat.spi.report.storage.ReportStorage;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

public class TransactionReportManagerTest extends ComponentTestCase {
	@BeforeClass
	public static void beforeClass() throws Exception {
		Files.forDir().delete(new File("target/report"), true);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testCheckpoint() throws Exception {
		lookup(ReportConfiguration.class).setBaseDataDir(new File("target"));

		ReportManagerManager rmm = lookup(ReportManagerManager.class);
		ReportDelegateManager delegateManager = lookup(ReportDelegateManager.class);
		ReportStorage<TransactionReport> storage = lookup(ReportStorage.class);

		ReportDelegate<TransactionReport> delegate = delegateManager.getDelegate(TransactionConstants.NAME);
		ReportManager<TransactionReport> rm = rmm.getReportManager(TransactionConstants.NAME);
		Date startTime = ReportPeriod.HOUR.getStartTime(new Date(1452313569760L));

		for (int i = 0; i < 2; i++) {
			TransactionReport r1 = rm.getLocalReport("test1", startTime, i, true);
			TransactionReport r2 = rm.getLocalReport("test2", startTime, i, true);

			r1.addIp("ip" + i);
			r2.addIp("IP" + i);

			rm.doCheckpoint(startTime, i, false);
		}

		List<TransactionReport> reports = storage.loadAll(delegate, ReportPeriod.HOUR, startTime, "test1");

		Assert.assertEquals(2, reports.size());

		List<TransactionReport> dailyReports = storage.loadAll(delegate, ReportPeriod.DAY, startTime, "test1");

		Assert.assertEquals(1, dailyReports.size());
		Assert.assertEquals("[ip0, ip1]", dailyReports.get(0).getIps().toString());
	}
}

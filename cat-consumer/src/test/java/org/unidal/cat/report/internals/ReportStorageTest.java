package org.unidal.cat.report.internals;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportPeriod;
import org.unidal.cat.report.spi.ReportDelegate;
import org.unidal.dal.jdbc.test.JdbcTestCase;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportContentDao;
import com.dianping.cat.core.dal.DailyReportContentEntity;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.HourlyReportEntity;

public class ReportStorageTest extends JdbcTestCase {
	@BeforeClass
	public static void beforeClass() throws Exception {
		Files.forDir().delete(new File("target/report"), true);
	}

	@Before
	public void before() throws Exception {
		createTables("report");
	}

	@Override
	protected String getDefaultDataSource() {
		return "cat";
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDefaultHourly() throws Exception {
		ReportStorage<Report> storage = lookup(ReportStorage.class);
		ReportDelegate<Report> delegate = lookup(ReportDelegate.class, TransactionConstants.NAME);
		Date startTime = new Date(1446885302848L); // Sat Nov 07 16:35:02 CST 2015

		// for hourly
		Date hourlyPeriod = ReportPeriod.HOUR.getStartTime(startTime);
		Report hourlyReport = new TransactionReport("default").setStartTime(hourlyPeriod);

		storage.store(delegate, ReportPeriod.HOUR, hourlyReport, ReportStoragePolicy.FILE_AND_MYSQL);
		List<Report> hourlyReports = storage.loadAll(delegate, ReportPeriod.HOUR, hourlyPeriod, hourlyReport.getDomain());

		Assert.assertEquals(1, hourlyReports.size());
		Assert.assertEquals(hourlyReport, hourlyReports.get(0));

		// for daily
		Date dailyPeriod = ReportPeriod.DAY.getStartTime(startTime);
		Report dailyReport = new TransactionReport("default").setStartTime(dailyPeriod);

		storage.store(delegate, ReportPeriod.DAY, dailyReport, ReportStoragePolicy.FILE_AND_MYSQL);
		List<Report> dailyReports = storage.loadAll(delegate, ReportPeriod.DAY, dailyPeriod, dailyReport.getDomain());

		Assert.assertEquals(1, dailyReports.size());
		Assert.assertEquals(dailyReport, dailyReports.get(0));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFileDaily() throws IOException {
		ReportStorage<Report> storage = lookup(ReportStorage.class, FileReportStorage.ID);
		ReportDelegate<Report> delegate = lookup(ReportDelegate.class, TransactionConstants.NAME);
		Date startTime = new Date(1446885302848L); // Sat Nov 07 16:35:02 CST 2015
		Date period = ReportPeriod.DAY.getStartTime(startTime);
		Report report = new TransactionReport("file").setStartTime(period);
		File dailyFile = ((FileReportStorage<Report>) storage).getDailyReportFile(delegate, period, report.getDomain());

		storage.store(delegate, ReportPeriod.DAY, report, ReportStoragePolicy.FILE_AND_MYSQL);

		Assert.assertEquals("target/report/2015-11/07/daily/transaction/file.xml", dailyFile.getPath());
		Assert.assertTrue(String.format("File(%s) has not been created.", dailyFile), dailyFile.exists());

		List<Report> reports = storage.loadAll(delegate, ReportPeriod.DAY, period, report.getDomain());

		Assert.assertEquals(1, reports.size());
		Assert.assertEquals(report, reports.get(0));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFileHourly() throws IOException {
		ReportStorage<Report> storage = lookup(ReportStorage.class, FileReportStorage.ID);
		ReportDelegate<Report> delegate = lookup(ReportDelegate.class, TransactionConstants.NAME);
		Date startTime = new Date(1446885302848L); // Sat Nov 07 16:35:02 CST 2015
		Date period = ReportPeriod.HOUR.getStartTime(startTime);
		Report report = new TransactionReport("file").setStartTime(period);
		File file = ((FileReportStorage<Report>) storage).getHourlyReportFile(delegate, period, report.getDomain());

		storage.store(delegate, ReportPeriod.HOUR, report, ReportStoragePolicy.FILE_AND_MYSQL);

		Assert.assertEquals("target/report/2015-11/07/16/transaction/file.xml", file.getPath());
		Assert.assertTrue(String.format("File(%s) has not been created.", file), file.exists());

		List<Report> reports = storage.loadAll(delegate, ReportPeriod.HOUR, period, report.getDomain());

		Assert.assertEquals(1, reports.size());
		Assert.assertEquals(report, reports.get(0));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMysqlDaily() throws Exception {
		ReportStorage<Report> storage = lookup(ReportStorage.class, MysqlReportStorage.ID);
		ReportDelegate<Report> delegate = lookup(ReportDelegate.class, TransactionConstants.NAME);
		Date startTime = new Date(1446885302848L); // Sat Nov 07 16:35:02 CST 2015
		Date period = ReportPeriod.DAY.getStartTime(startTime);
		Report report = new TransactionReport("mysql").setStartTime(period);

		storage.store(delegate, ReportPeriod.DAY, report, ReportStoragePolicy.FILE_AND_MYSQL);

		DailyReportDao dao = lookup(DailyReportDao.class);
		DailyReportContentDao contentDao = lookup(DailyReportContentDao.class);
		DailyReport dr = dao.findByDomainNamePeriod(report.getDomain(), delegate.getName(), period,
		      DailyReportEntity.READSET_FULL);

		Assert.assertNotNull(contentDao.findByPK(dr.getId(), DailyReportContentEntity.READSET_FULL));

		List<Report> reports = storage.loadAll(delegate, ReportPeriod.DAY, period, report.getDomain());

		Assert.assertEquals(1, reports.size());
		Assert.assertEquals(report, reports.get(0));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMysqlHourly() throws Exception {
		ReportStorage<Report> storage = lookup(ReportStorage.class, MysqlReportStorage.ID);
		ReportDelegate<Report> delegate = lookup(ReportDelegate.class, TransactionConstants.NAME);
		Date startTime = new Date(1446885302848L); // Sat Nov 07 16:35:02 CST 2015
		Date period = ReportPeriod.HOUR.getStartTime(startTime);
		Report report = new TransactionReport("mysql").setStartTime(period);

		storage.store(delegate, ReportPeriod.HOUR, report, ReportStoragePolicy.FILE_AND_MYSQL);

		HourlyReportDao dao = lookup(HourlyReportDao.class);
		HourlyReportContentDao contentDao = lookup(HourlyReportContentDao.class);
		List<HourlyReport> all = dao.findAllByDomainNamePeriod(period, report.getDomain(), delegate.getName(),
		      HourlyReportEntity.READSET_FULL);

		Assert.assertEquals(1, all.size());
		Assert.assertNotNull(contentDao.findByPK(all.get(0).getId(), HourlyReportContentEntity.READSET_FULL));

		List<Report> reports = storage.loadAll(delegate, ReportPeriod.HOUR, period, report.getDomain());

		Assert.assertEquals(1, reports.size());
		Assert.assertEquals(report, reports.get(0));
	}
}

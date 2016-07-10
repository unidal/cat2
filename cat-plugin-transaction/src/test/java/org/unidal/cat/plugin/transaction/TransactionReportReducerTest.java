package org.unidal.cat.plugin.transaction;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.ReportStoragePolicy;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.storage.ReportStorage;
import org.unidal.cat.spi.report.task.ReportTask;
import org.unidal.cat.spi.report.task.ReportTaskExecutor;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

public class TransactionReportReducerTest extends ComponentTestCase {
	@Before
	public void before() throws Exception {
		defineComponent(ReportStorage.class, MockReportStorage.class);
	}

	@Test
	public void testDay() throws Exception {
		ReportTask task = new MockReportTask(ReportPeriod.HOUR, ReportPeriod.DAY);

		lookup(ReportTaskExecutor.class).execute(task);
	}

	@Test
	public void testWeek() throws Exception {
		ReportTask task = new MockReportTask(ReportPeriod.DAY, ReportPeriod.WEEK);

		lookup(ReportTaskExecutor.class).execute(task);
	}

	@Test
	public void testMonth() throws Exception {
		ReportTask task = new MockReportTask(ReportPeriod.DAY, ReportPeriod.MONTH);

		lookup(ReportTaskExecutor.class).execute(task);
	}

	public static class MockReportStorage implements ReportStorage<TransactionReport> {
		@Override
		public List<TransactionReport> loadAll(ReportDelegate<TransactionReport> delegate, ReportPeriod period,
		      Date startTime, String domain) throws IOException {
			List<TransactionReport> reports = new ArrayList<TransactionReport>();

			for (int i = 1; i <= 3; i++) {
				TransactionReport report = loadReport(delegate, period.getName() + "-" + i + ".xml");

				reports.add(report);
			}

			return reports;
		}

		private TransactionReport loadReport(ReportDelegate<TransactionReport> delegate, String resource)
		      throws IOException {
			InputStream in = getClass().getResourceAsStream("reducer/" + resource);

			if (in == null) {
				throw new IllegalStateException(String.format("Unable to load resource(%s)!", "reducer/" + resource));
			}

			String xml = Files.forIO().readFrom(in, "utf-8");
			TransactionReport report = delegate.parseXml(xml);

			return report;
		}

		@Override
		public void store(ReportDelegate<TransactionReport> delegate, ReportPeriod period, TransactionReport report,
		      int index, ReportStoragePolicy policy) throws IOException {
			TransactionReport expected = loadReport(delegate, period.getName() + ".xml");

			Assert.assertEquals(String.format("TransactionReport(%s) mismatched!", period), expected.toString(),
			      report.toString());
		}
	}

	static class MockReportTask implements ReportTask {
		private ReportPeriod m_source;

		private ReportPeriod m_target;

		private List<String> m_domains = new ArrayList<String>();

		public MockReportTask(ReportPeriod source, ReportPeriod target) {
			m_source = source;
			m_target = target;

			m_domains.add("cat");
		}

		@Override
		public void done(String domain) {
		}

		@Override
		public List<String> getDomains() {
			return m_domains;
		}

		@Override
		public int getFailureCount() {
			return 0;
		}

		@Override
		public int getId() {
			return 0;
		}

		@Override
		public String getReportName() {
			return TransactionConstants.NAME;
		}

		@Override
		public ReportPeriod getSourcePeriod() {
			return m_source;
		}

		@Override
		public Date getSourceStartTime() {
			return m_source.getStartTime(new Date());
		}

		@Override
		public ReportPeriod getTargetPeriod() {
			return m_target;
		}
	}
}

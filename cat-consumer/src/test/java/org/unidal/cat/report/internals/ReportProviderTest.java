package org.unidal.cat.report.internals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportConfiguration;
import org.unidal.cat.report.ReportPeriod;
import org.unidal.cat.report.spi.ReportDelegate;
import org.unidal.cat.report.spi.internals.DefaultRemoteContext;
import org.unidal.cat.report.spi.remote.RemoteContext;
import org.unidal.cat.report.spi.remote.RemoteStub;
import org.unidal.dal.jdbc.test.JdbcTestCase;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultNativeBuilder;

public class ReportProviderTest extends JdbcTestCase {
	private static Set<String> s_servers = new TreeSet<String>();

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
	public void testDefault() throws IOException {
		ReportProvider<Report> provider = lookup(ReportProvider.class);
		ReportDelegate<Report> delegate = lookup(ReportDelegate.class, TransactionConstants.NAME);
		ReportStorage<Report> storage = lookup(ReportStorage.class);
		Date startTime = new Date(1446885302848L); // Sat Nov 07 16:35:02 CST 2015
		Report report = new TransactionReport("default").setStartTime(startTime);

		storage.store(delegate, ReportPeriod.HOUR, report, ReportStoragePolicy.FILE_AND_MYSQL);

		RemoteContext ctx = new DefaultRemoteContext(delegate.getName(), report.getDomain(), startTime,
		      ReportPeriod.HOUR, null);

		Assert.assertEquals(true, provider.isEligible(ctx, delegate));
		Assert.assertEquals(report, provider.getReport(ctx, delegate));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDefault2() throws Exception {
		defineComponent(RemoteStub.class, MockRemoteStub.class);
		defineComponent(ReportConfiguration.class, MockReportConfiguration.class);

		ReportProvider<Report> provider = lookup(ReportProvider.class);
		ReportDelegate<Report> delegate = lookup(ReportDelegate.class, TransactionConstants.NAME);
		ReportStorage<Report> storage = lookup(ReportStorage.class);
		Date startTime = new Date();
		Report report = new TransactionReport("default2").setStartTime(startTime);

		storage.store(delegate, ReportPeriod.HOUR, report, ReportStoragePolicy.FILE_AND_MYSQL);

		RemoteContext ctx = new DefaultRemoteContext(delegate.getName(), report.getDomain(), startTime,
		      ReportPeriod.HOUR, null);

		Assert.assertEquals(true, provider.isEligible(ctx, delegate));
		Assert.assertEquals(report, provider.getReport(ctx, delegate));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testHistorical() throws IOException {
		ReportProvider<Report> provider = lookup(ReportProvider.class, HistoricalReportProvider.ID);
		ReportDelegate<Report> delegate = lookup(ReportDelegate.class, TransactionConstants.NAME);
		ReportStorage<Report> storage = lookup(ReportStorage.class);
		Date startTime = new Date(1446885302848L);
		Report report = new TransactionReport("historical").setStartTime(startTime);

		storage.store(delegate, ReportPeriod.HOUR, report, ReportStoragePolicy.FILE_AND_MYSQL);

		RemoteContext ctx = new DefaultRemoteContext(delegate.getName(), report.getDomain(), startTime,
		      ReportPeriod.HOUR, null);

		Assert.assertEquals(true, provider.isEligible(ctx, delegate));
		Assert.assertEquals(report, provider.getReport(ctx, delegate));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testRecent() throws Exception {
		defineComponent(RemoteStub.class, MockRemoteStub.class);
		defineComponent(ReportConfiguration.class, MockReportConfiguration.class);

		ReportProvider<Report> provider = lookup(ReportProvider.class, RecentReportProvider.ID);
		ReportDelegate<Report> delegate = lookup(ReportDelegate.class, TransactionConstants.NAME);
		Date startTime = new Date();
		Report report = new TransactionReport("recent").setStartTime(startTime);

		s_servers.clear();

		RemoteContext ctx = new DefaultRemoteContext(delegate.getName(), report.getDomain(), startTime,
		      ReportPeriod.HOUR, null);

		Assert.assertEquals(true, provider.isEligible(ctx, delegate));
		Assert.assertEquals(report, provider.getReport(ctx, delegate));
		Assert.assertEquals("[127.0.0.1, 127.0.0.3]", s_servers.toString());
	}

	public static final class MockRemoteStub implements RemoteStub {
		@Override
		public InputStream getReport(RemoteContext ctx, String server) throws IOException {
			TransactionReport report = new TransactionReport(ctx.getDomain()).setStartTime(ctx.getStartTime());
			byte[] data = DefaultNativeBuilder.build(report);

			s_servers.add(server);
			return new ByteArrayInputStream(data);
		}
	}

	public static final class MockReportConfiguration extends DefaultReportConfiguration {
		@Override
		public Map<String, Boolean> getServers() {
			return new HashMap<String, Boolean>() {
				private static final long serialVersionUID = 1L;

				{
					put("127.0.0.1", true);
					put("127.0.0.2", false);
					put("127.0.0.3", true);
				}
			};
		}
	}
}

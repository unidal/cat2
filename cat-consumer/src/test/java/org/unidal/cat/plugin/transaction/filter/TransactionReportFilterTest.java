package org.unidal.cat.plugin.transaction.filter;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.remote.DefaultRemoteContext;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

public class TransactionReportFilterTest extends ComponentTestCase {
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
	private TransactionReport screen(String filterId, ReportPeriod period, TransactionReport report, String... args) {
		ReportFilter<TransactionReport> filter = lookup(ReportFilter.class, TransactionConstants.NAME + ":" + filterId);
		RemoteContext ctx = new DefaultRemoteContext(TransactionConstants.NAME, report.getDomain(),
		      report.getStartTime(), period, filter);

		if (args.length % 2 != 0) {
			throw new IllegalArgumentException("args should be paired!");
		}

		for (int i = 0; i < args.length; i += 2) {
			String property = args[i];
			String value = args[i + 1];

			ctx.setProperty(property, value);
		}

		return filter.screen(ctx, report);
	}

	@SuppressWarnings("unchecked")
	private void tailor(String filterId, ReportPeriod period, TransactionReport report, String... args) {
		ReportFilter<TransactionReport> filter = lookup(ReportFilter.class, TransactionConstants.NAME + ":" + filterId);
		RemoteContext ctx = new DefaultRemoteContext(TransactionConstants.NAME, report.getDomain(),
		      report.getStartTime(), period, filter);

		if (args.length % 2 != 0) {
			throw new IllegalArgumentException("args should be paired!");
		}

		for (int i = 0; i < args.length; i += 2) {
			String property = args[i];
			String value = args[i + 1];

			ctx.setProperty(property, value);
		}

		filter.tailor(ctx, report);
	}

	@Test
	public void testName() throws Exception {
		TransactionReport expected = loadReport("name.xml");
		TransactionReport source = loadReport("source.xml");
		TransactionReport report = loadReport("source.xml");

		TransactionReport screened = screen(TransactionNameFilter.ID, ReportPeriod.HOUR, report, //
		      "type", "URL");
		Assert.assertEquals(source.toString(), report.toString());
		Assert.assertEquals(expected.toString(), screened.toString());

		tailor(TransactionNameFilter.ID, ReportPeriod.HOUR, report, //
		      "type", "URL");
		Assert.assertEquals(expected.toString(), report.toString());
	}

	@Test
	public void testGraphName() throws Exception {
		TransactionReport expected = loadReport("graph-name.xml");
		TransactionReport source = loadReport("source.xml");
		TransactionReport report = loadReport("source.xml");

		TransactionReport screened = screen(TransactionNameGraphFilter.ID, ReportPeriod.HOUR, report, //
		      "type", "URL", "name", "/cat/r/t");
		Assert.assertEquals(source.toString(), report.toString());
		Assert.assertEquals(expected.toString(), screened.toString());

		tailor(TransactionNameGraphFilter.ID, ReportPeriod.HOUR, report, //
		      "type", "URL", "name", "/cat/r/t");
		Assert.assertEquals(expected.toString(), report.toString());
	}
	
	@Test
	public void testGraphNameWithIp() throws Exception {
		TransactionReport expected = loadReport("graph-name-ip.xml");
		TransactionReport source = loadReport("source.xml");
		TransactionReport report = loadReport("source.xml");
		
		TransactionReport screened = screen(TransactionNameGraphFilter.ID, ReportPeriod.HOUR, report, //
				"type", "URL", "name", "/cat/r/t", "ip", "192.168.31.158");
		Assert.assertEquals(source.toString(), report.toString());
		Assert.assertEquals(expected.toString(), screened.toString());
		
		tailor(TransactionNameGraphFilter.ID, ReportPeriod.HOUR, report, //
				"type", "URL", "name", "/cat/r/t", "ip", "192.168.31.158");
		Assert.assertEquals(expected.toString(), report.toString());
	}

	@Test
	public void testNameWithIp() throws Exception {
		TransactionReport expected = loadReport("name-ip.xml");
		TransactionReport source = loadReport("source.xml");
		TransactionReport report = loadReport("source.xml");

		TransactionReport screened = screen(TransactionNameFilter.ID, ReportPeriod.HOUR, report, //
		      "type", "URL", "ip", "192.168.31.158");
		Assert.assertEquals(source.toString(), report.toString());
		Assert.assertEquals(expected.toString(), screened.toString());

		tailor(TransactionNameFilter.ID, ReportPeriod.HOUR, report, //
		      "type", "URL", "ip", "192.168.31.158");
		Assert.assertEquals(expected.toString(), report.toString());
	}

	@Test
	public void testType() throws Exception {
		TransactionReport expected = loadReport("type.xml");
		TransactionReport source = loadReport("source.xml");
		TransactionReport report = loadReport("source.xml");

		TransactionReport screened = screen(TransactionTypeFilter.ID, ReportPeriod.HOUR, report);
		Assert.assertEquals(source.toString(), report.toString());
		Assert.assertEquals(expected.toString(), screened.toString());

		tailor(TransactionTypeFilter.ID, ReportPeriod.HOUR, report);
		Assert.assertEquals(expected.toString(), report.toString());
	}

	@Test
	public void testGraphType() throws Exception {
		TransactionReport expected = loadReport("graph-type.xml");
		TransactionReport source = loadReport("source.xml");
		TransactionReport report = loadReport("source.xml");

		TransactionReport screened = screen(TransactionTypeGraphFilter.ID, ReportPeriod.HOUR, report, //
		      "type", "URL");
		Assert.assertEquals(source.toString(), report.toString());
		Assert.assertEquals(expected.toString(), screened.toString());

		tailor(TransactionTypeGraphFilter.ID, ReportPeriod.HOUR, report, //
		      "type", "URL");
		Assert.assertEquals(expected.toString(), report.toString());
	}
	
	@Test
	public void testGraphTypeWithIp() throws Exception {
		TransactionReport expected = loadReport("graph-type-ip.xml");
		TransactionReport source = loadReport("source.xml");
		TransactionReport report = loadReport("source.xml");
		
		TransactionReport screened = screen(TransactionTypeGraphFilter.ID, ReportPeriod.HOUR, report, //
				"type", "URL", "ip", "192.168.31.158");
		Assert.assertEquals(source.toString(), report.toString());
		Assert.assertEquals(expected.toString(), screened.toString());
		
		tailor(TransactionTypeGraphFilter.ID, ReportPeriod.HOUR, report, //
				"type", "URL", "ip", "192.168.31.158");
		Assert.assertEquals(expected.toString(), report.toString());
	}

	@Test
	public void testTypeWithIp() throws Exception {
		TransactionReport expected = loadReport("type-ip.xml");
		TransactionReport source = loadReport("source.xml");
		TransactionReport report = loadReport("source.xml");

		TransactionReport screened = screen(TransactionTypeFilter.ID, ReportPeriod.HOUR, report, //
		      "ip", "192.168.31.158");
		Assert.assertEquals(source.toString(), report.toString());
		Assert.assertEquals(expected.toString(), screened.toString());

		tailor(TransactionTypeFilter.ID, ReportPeriod.HOUR, report, //
		      "ip", "192.168.31.158");
		Assert.assertEquals(expected.toString(), report.toString());
	}
}

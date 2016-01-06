package org.unidal.cat.report;

import java.util.Date;
import java.util.TimeZone;

import junit.framework.Assert;

import org.junit.Test;

public class ReportPeriodTest {
	private long HOUR = 3600 * 1000L;

	@Test
	public void testCurrentHour() {
		long now = System.currentTimeMillis();
		Date startTime = new Date(now - now % HOUR);

		Assert.assertEquals(false, ReportPeriod.HOUR.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.DAY.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.WEEK.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.MONTH.isHistorical(startTime));

		Assert.assertEquals(true, ReportPeriod.HOUR.isCurrent(startTime));
		Assert.assertEquals(true, ReportPeriod.DAY.isCurrent(startTime));
		Assert.assertEquals(true, ReportPeriod.WEEK.isCurrent(startTime));
		Assert.assertEquals(true, ReportPeriod.MONTH.isCurrent(startTime));
	}

	@Test
	public void testLastHour() {
		long now = System.currentTimeMillis();
		Date startTime = new Date(now - now % HOUR - HOUR);

		Assert.assertEquals(false, ReportPeriod.HOUR.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.DAY.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.WEEK.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.MONTH.isHistorical(startTime));

		Assert.assertEquals(false, ReportPeriod.HOUR.isCurrent(startTime));
		Assert.assertEquals(true, ReportPeriod.DAY.isCurrent(startTime));
		Assert.assertEquals(true, ReportPeriod.WEEK.isCurrent(startTime));
		Assert.assertEquals(true, ReportPeriod.MONTH.isCurrent(startTime));
	}

	@Test
	public void testLastLastHour() {
		long now = System.currentTimeMillis();
		Date startTime = new Date(now - now % HOUR - 2 * HOUR);

		Assert.assertEquals(true, ReportPeriod.HOUR.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.DAY.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.WEEK.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.MONTH.isHistorical(startTime));

		Assert.assertEquals(false, ReportPeriod.HOUR.isCurrent(startTime));
		Assert.assertEquals(true, ReportPeriod.DAY.isCurrent(startTime));
		Assert.assertEquals(true, ReportPeriod.WEEK.isCurrent(startTime));
		Assert.assertEquals(true, ReportPeriod.MONTH.isCurrent(startTime));
	}

	@Test
	public void testIsCurrent() {
		// Sat Nov 07 16:35:02 CST 2015
		Date startTime = new Date(1446885302848L);

		Assert.assertEquals(false, ReportPeriod.HOUR.isCurrent(startTime));
		Assert.assertEquals(false, ReportPeriod.DAY.isCurrent(startTime));
		Assert.assertEquals(false, ReportPeriod.WEEK.isCurrent(startTime));
		Assert.assertEquals(false, ReportPeriod.MONTH.isCurrent(startTime));
	}

	@Test
	public void testStartTime() {
		// Sat Nov 07 16:35:02 CST 2015
		Date date = new Date(1446885302848L);

		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		Assert.assertEquals("Sat Nov 07 16:00:00 CST 2015", ReportPeriod.HOUR.getStartTime(date).toString());
		Assert.assertEquals("Sat Nov 07 00:00:00 CST 2015", ReportPeriod.DAY.getStartTime(date).toString());
		Assert.assertEquals("Sun Nov 01 00:00:00 CST 2015", ReportPeriod.WEEK.getStartTime(date).toString());
		Assert.assertEquals("Sun Nov 01 00:00:00 CST 2015", ReportPeriod.MONTH.getStartTime(date).toString());
	}

	@Test
	public void testStartTime2() {
		// Thu Oct 29 20:35:02 CST 2015
		Date date = new Date(1446123402848L);

		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		Assert.assertEquals("Thu Oct 29 20:00:00 CST 2015", ReportPeriod.HOUR.getStartTime(date).toString());
		Assert.assertEquals("Thu Oct 29 00:00:00 CST 2015", ReportPeriod.DAY.getStartTime(date).toString());
		Assert.assertEquals("Sun Oct 25 00:00:00 CST 2015", ReportPeriod.WEEK.getStartTime(date).toString());
		Assert.assertEquals("Thu Oct 01 00:00:00 CST 2015", ReportPeriod.MONTH.getStartTime(date).toString());
	}
}

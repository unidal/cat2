package org.unidal.cat.spi;

import java.util.Date;
import java.util.TimeZone;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.spi.ReportPeriod;

public class ReportPeriodTest {
	private long HOUR = 3600 * 1000L;

	@Test
	public void formatAndParse() {
		// Sat Nov 07 16:35:02 CST 2015
		Date date = new Date(1446885302848L);

		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		Assert.assertEquals("2015110716", ReportPeriod.HOUR.format(date));
		Assert.assertEquals("20151107", ReportPeriod.DAY.format(date));
		Assert.assertEquals("20151107", ReportPeriod.WEEK.format(date));
		Assert.assertEquals("201511", ReportPeriod.MONTH.format(date));

		Assert.assertEquals("Sat Nov 07 16:00:00 CST 2015", ReportPeriod.HOUR.parse("2015110716", null).toString());
		Assert.assertEquals("Sat Nov 07 00:00:00 CST 2015", ReportPeriod.DAY.parse("20151107", null).toString());
		Assert.assertEquals("Sat Nov 07 00:00:00 CST 2015", ReportPeriod.WEEK.parse("20151107", null).toString());
		Assert.assertEquals("Sun Nov 01 00:00:00 CST 2015", ReportPeriod.MONTH.parse("201511", null).toString());
	}

	@Test
	public void getBaselineStartTime() {
		// Sat Nov 07 16:35:02 CST 2015
		Date date = new Date(1446885302848L);
		
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		Assert.assertEquals("Fri Nov 06 16:00:00 CST 2015", ReportPeriod.HOUR.getBaselineStartTime(date).toString());
		Assert.assertEquals("Sat Oct 31 00:00:00 CST 2015", ReportPeriod.DAY.getBaselineStartTime(date).toString());
		Assert.assertEquals("Sun Oct 18 00:00:00 CST 2015", ReportPeriod.WEEK.getBaselineStartTime(date).toString());
		Assert.assertEquals("Sat Nov 01 00:00:00 CST 2014", ReportPeriod.MONTH.getBaselineStartTime(date).toString());
	}
	
	@Test
	public void getByName() {
		Assert.assertEquals(ReportPeriod.HOUR, ReportPeriod.getByName("hour", null));
		Assert.assertEquals(ReportPeriod.DAY, ReportPeriod.getByName("day", null));
		Assert.assertEquals(ReportPeriod.WEEK, ReportPeriod.getByName("week", null));
		Assert.assertEquals(ReportPeriod.MONTH, ReportPeriod.getByName("month", null));
		Assert.assertEquals(null, ReportPeriod.getByName("unknown", null));
	}
	
	@Test
	public void getLastStartTime() {
		// Sat Nov 07 16:35:02 CST 2015
		Date date = new Date(1446885302848L);
		
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		Assert.assertEquals("Sat Nov 07 15:00:00 CST 2015", ReportPeriod.HOUR.getLastStartTime(date).toString());
		Assert.assertEquals("Fri Nov 06 00:00:00 CST 2015", ReportPeriod.DAY.getLastStartTime(date).toString());
		Assert.assertEquals("Sun Oct 25 00:00:00 CST 2015", ReportPeriod.WEEK.getLastStartTime(date).toString());
		Assert.assertEquals("Thu Oct 01 00:00:00 CST 2015", ReportPeriod.MONTH.getLastStartTime(date).toString());
	}
	
	@Test
	public void getNextStartTime() {
		// Sat Nov 07 16:35:02 CST 2015
		Date date = new Date(1446885302848L);
		
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		Assert.assertEquals("Sat Nov 07 17:00:00 CST 2015", ReportPeriod.HOUR.getNextStartTime(date).toString());
		Assert.assertEquals("Sun Nov 08 00:00:00 CST 2015", ReportPeriod.DAY.getNextStartTime(date).toString());
		Assert.assertEquals("Sun Nov 08 00:00:00 CST 2015", ReportPeriod.WEEK.getNextStartTime(date).toString());
		Assert.assertEquals("Tue Dec 01 00:00:00 CST 2015", ReportPeriod.MONTH.getNextStartTime(date).toString());
	}

	@Test
	public void getStartTime() {
		// Sat Nov 07 16:35:02 CST 2015
		Date date = new Date(1446885302848L);

		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		Assert.assertEquals("Sat Nov 07 16:00:00 CST 2015", ReportPeriod.HOUR.getStartTime(date).toString());
		Assert.assertEquals("Sat Nov 07 00:00:00 CST 2015", ReportPeriod.DAY.getStartTime(date).toString());
		Assert.assertEquals("Sun Nov 01 00:00:00 CST 2015", ReportPeriod.WEEK.getStartTime(date).toString());
		Assert.assertEquals("Sun Nov 01 00:00:00 CST 2015", ReportPeriod.MONTH.getStartTime(date).toString());
	}
	
	@Test
	public void getReduceTime() {
		// Sat Nov 07 16:35:02 CST 2015
		Date date = new Date(1446885302848L);
		
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		Assert.assertEquals("Sat Nov 07 17:00:00 CST 2015", ReportPeriod.HOUR.getReduceTime(date).toString());
		Assert.assertEquals("Sat Nov 07 01:00:00 CST 2015", ReportPeriod.DAY.getReduceTime(date).toString());
		Assert.assertEquals("Sun Nov 08 01:00:00 CST 2015", ReportPeriod.WEEK.getReduceTime(date).toString());
		Assert.assertEquals("Tue Dec 01 02:00:00 CST 2015", ReportPeriod.MONTH.getReduceTime(date).toString());
	}

	@Test
	public void getStartTime2() {
		// Thu Oct 29 20:35:02 CST 2015
		Date date = new Date(1446123402848L);

		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		Assert.assertEquals("Thu Oct 29 20:00:00 CST 2015", ReportPeriod.HOUR.getStartTime(date).toString());
		Assert.assertEquals("Thu Oct 29 00:00:00 CST 2015", ReportPeriod.DAY.getStartTime(date).toString());
		Assert.assertEquals("Sun Oct 25 00:00:00 CST 2015", ReportPeriod.WEEK.getStartTime(date).toString());
		Assert.assertEquals("Thu Oct 01 00:00:00 CST 2015", ReportPeriod.MONTH.getStartTime(date).toString());
	}

	@Test
	public void isCurrent() {
		// Sat Nov 07 16:35:02 CST 2015
		Date startTime = new Date(1446885302848L);

		Assert.assertEquals(false, ReportPeriod.HOUR.isCurrent(startTime));
		Assert.assertEquals(false, ReportPeriod.DAY.isCurrent(startTime));
		Assert.assertEquals(false, ReportPeriod.WEEK.isCurrent(startTime));
		Assert.assertEquals(false, ReportPeriod.MONTH.isCurrent(startTime));
	}

	@Test
	public void isCurrent2() {
		long now = System.currentTimeMillis();
		Date startTime = new Date(now - now % HOUR - HOUR);

		Assert.assertEquals(false, ReportPeriod.HOUR.isCurrent(startTime));
		Assert.assertEquals(true, ReportPeriod.DAY.isCurrent(startTime));
		Assert.assertEquals(true, ReportPeriod.WEEK.isCurrent(startTime));
		Assert.assertEquals(true, ReportPeriod.MONTH.isCurrent(startTime));
	}

	@Test
	public void isHistorical() {
		long now = System.currentTimeMillis();
		Date startTime = new Date(now - now % HOUR);

		Assert.assertEquals(false, ReportPeriod.HOUR.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.DAY.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.WEEK.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.MONTH.isHistorical(startTime));
	}

	@Test
	public void isHistorical2() {
		long now = System.currentTimeMillis();
		Date startTime = new Date(now - now % HOUR - HOUR);

		Assert.assertEquals(false, ReportPeriod.HOUR.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.DAY.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.WEEK.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.MONTH.isHistorical(startTime));
	}

	@Test
	public void isHistorical3() {
		long now = System.currentTimeMillis();
		Date startTime = new Date(now - now % HOUR - 2 * HOUR);

		Assert.assertEquals(true, ReportPeriod.HOUR.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.DAY.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.WEEK.isHistorical(startTime));
		Assert.assertEquals(true, ReportPeriod.MONTH.isHistorical(startTime));
	}
}

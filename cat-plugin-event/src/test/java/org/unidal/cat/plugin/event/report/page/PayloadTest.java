package org.unidal.cat.plugin.event.report.page;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.core.report.CoreReportPayload;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.helper.Dates;
import org.unidal.helper.Reflects;

public class PayloadTest {
	@Test
	public void testPeriod() {
		Payload payload = new Payload();

		payload.setPeriod("");
		payload.validate(null);

		Assert.assertEquals(ReportPeriod.HOUR, payload.getPeriod());

		payload.setPeriod("hour");
		payload.validate(null);

		Assert.assertEquals(ReportPeriod.HOUR, payload.getPeriod());

		payload.setPeriod("day");
		payload.validate(null);

		Assert.assertEquals(ReportPeriod.DAY, payload.getPeriod());

		payload.setPeriod("unknown");
		payload.validate(null);

		Assert.assertEquals(ReportPeriod.HOUR, payload.getPeriod());
	}

	private String format(Date date) {
		return Dates.from(date).asString("yyyy-MM-dd HH:mm:ss");
	}

	@Test
	public void testStartTime() {
		Payload payload = new Payload();

		Reflects.forField().setDeclaredFieldValue(CoreReportPayload.class, "m_date", payload, 2016073115L);

		payload.validate(null);

		Assert.assertEquals("2016-07-31 15:00:00", format(payload.getStartTime()));
		Assert.assertEquals("2016-07-31 15:59:59", format(payload.getEndTime()));

		payload.setPeriod("day");
		payload.validate(null);

		Assert.assertEquals("2016-07-31 00:00:00", format(payload.getStartTime()));
		Assert.assertEquals("2016-07-31 23:59:59", format(payload.getEndTime()));

		payload.setPeriod("month");
		payload.validate(null);

		Assert.assertEquals("2016-07-01 00:00:00", format(payload.getStartTime()));
		Assert.assertEquals("2016-07-31 23:59:59", format(payload.getEndTime()));
	}
}

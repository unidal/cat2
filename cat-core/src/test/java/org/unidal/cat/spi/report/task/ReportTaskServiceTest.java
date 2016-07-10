package org.unidal.cat.spi.report.task;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.dal.jdbc.test.JdbcTestCase;
import org.unidal.helper.Inets;

public class ReportTaskServiceTest extends JdbcTestCase {
	@Before
	public void before() throws Exception {
		createTables("report2");
	}

	@Override
	protected String getDefaultDataSource() {
		return "cat";
	}

	@Test
	public void test() throws Exception {
		ReportTaskService service = lookup(ReportTaskService.class);
		Date startTime = ReportPeriod.DAY.getStartTime(new Date());
		String ip = Inets.IP4.getLocalHostAddress();

		// add a task during hourly report checkpoint
		service.add(ip, ReportPeriod.DAY, startTime, "mock", ReportPeriod.DAY.getReduceTime(startTime));

		// success to claim a task for first time
		ReportTask t1 = service.pull(ip);
		Assert.assertNotNull(t1);

		// failed to claim a task for second time
		ReportTask t2 = service.pull("other");
		Assert.assertNull(t2);

		// complete it
		service.complete(t1);

		// can't be claimed again
		ReportTask t3 = service.pull(ip);
		Assert.assertNull(t3);
	}
}

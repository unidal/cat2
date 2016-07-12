package org.unidal.cat.dal.report;

import org.junit.Before;
import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.test.JdbcTestCase;

public class HourlyReportDaoTest extends JdbcTestCase {
	@Before
   public void before() throws Exception {
      createTables("report2");
   }
	
	@Override
	protected String getDefaultDataSource() {
		return "cat";
	}

	@Test
	public void test() throws DalException {
		HourlyReportDao dao = lookup(HourlyReportDao.class);
		HourlyReportDo report = dao.createLocal();
		
		System.out.println(report);
		dao.insert(report);
	}
}

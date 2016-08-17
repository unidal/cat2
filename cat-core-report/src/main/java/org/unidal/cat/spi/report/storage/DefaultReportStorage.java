package org.unidal.cat.spi.report.storage;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportStorage.class)
public class DefaultReportStorage<T extends Report> implements ReportStorage<T> {
	@Inject(MysqlReportStorage.ID)
	private ReportStorage<T> m_mysql;

	@Override
	public List<T> loadAll(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain)
	      throws IOException {
		return m_mysql.loadAll(delegate, period, startTime, domain);
	}

	@Override
	public List<T> loadAllByDateRange(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, Date endTime,
	      String domain) throws IOException {
		return m_mysql.loadAllByDateRange(delegate, period, startTime, endTime, domain);
	}

	@Override
	public void store(ReportDelegate<T> delegate, ReportPeriod period, T report, int index) throws IOException {
		m_mysql.store(delegate, period, report, index);
	}
}

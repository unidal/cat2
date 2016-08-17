package org.unidal.cat.spi.report.storage;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportStorage.class, value = MysqlReportStorage.ID)
public class MysqlReportStorage<T extends Report> implements ReportStorage<T> {
	public static final String ID = "mysql";

	@Inject(MysqlHourlyReportStorage.ID)
	private ReportStorage<T> m_hourlyStorage;

	@Inject(MysqlHistoryReportStorage.ID)
	private ReportStorage<T> m_historyStorage;

	@Override
	public List<T> loadAll(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain)
	      throws IOException {
		switch (period) {
		case HOUR:
			return m_hourlyStorage.loadAll(delegate, period, startTime, domain);
		default:
			return m_historyStorage.loadAll(delegate, period, startTime, domain);
		}
	}

	@Override
	public List<T> loadAllByDateRange(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, Date endTime,
	      String domain) throws IOException {
		switch (period) {
		case HOUR:
			return m_hourlyStorage.loadAllByDateRange(delegate, period, startTime, endTime, domain);
		default:
			return m_historyStorage.loadAllByDateRange(delegate, period, startTime, endTime, domain);
		}
	}

	@Override
	public void store(ReportDelegate<T> delegate, ReportPeriod period, T report, int index)
	      throws IOException {
		switch (period) {
		case HOUR:
			m_hourlyStorage.store(delegate, period, report, index);
			break;
		default:
			m_historyStorage.store(delegate, period, report, index);
			break;
		}
	}
}

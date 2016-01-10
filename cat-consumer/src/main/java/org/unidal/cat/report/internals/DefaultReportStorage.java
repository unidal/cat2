package org.unidal.cat.report.internals;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportConfiguration;
import org.unidal.cat.report.ReportPeriod;
import org.unidal.cat.report.spi.ReportDelegate;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportStorage.class)
public class DefaultReportStorage<T extends Report> implements ReportStorage<T> {
	@Inject
	private ReportConfiguration m_configuration;

	@Inject(MysqlReportStorage.ID)
	private ReportStorage<T> m_mysql;

	@Inject(FileReportStorage.ID)
	private ReportStorage<T> m_file;

	@Override
	public List<T> loadAll(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain)
	      throws IOException {
		if (m_configuration.isLocalMode()) {
			return m_file.loadAll(delegate, period, startTime, domain);
		} else {
			return m_mysql.loadAll(delegate, period, startTime, domain);
		}
	}

	@Override
	public void store(ReportDelegate<T> delegate, ReportPeriod period, T report, int index, ReportStoragePolicy policy)
	      throws IOException {
		m_file.store(delegate, period, report, index, policy);

		if (!m_configuration.isLocalMode()) {
			m_mysql.store(delegate, period, report, index, policy);
		}
	}
}

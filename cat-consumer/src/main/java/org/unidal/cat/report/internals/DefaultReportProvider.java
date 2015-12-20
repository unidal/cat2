package org.unidal.cat.report.internals;

import java.io.IOException;
import java.util.Date;

import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportFilter;
import org.unidal.cat.report.ReportPeriod;
import org.unidal.cat.report.spi.ReportDelegate;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportProvider.class)
public class DefaultReportProvider<T extends Report> implements ReportProvider<T> {
	@Inject(RecentReportProvider.ID)
	private ReportProvider<T> m_rencent;

	@Inject(HistoricalReportProvider.ID)
	private ReportProvider<T> m_historical;

	@Override
	public boolean isEligible(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain) {
		return true;
	}

	@Override
	public T getReport(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain,
	      ReportFilter<T> filter) throws IOException {
		if (period.isHistorical(startTime)) {
			return m_historical.getReport(delegate, period, startTime, domain, filter);
		} else {
			return m_rencent.getReport(delegate, period, startTime, domain, filter);
		}
	}
}

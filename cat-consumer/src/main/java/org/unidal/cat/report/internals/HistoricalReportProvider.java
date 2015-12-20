package org.unidal.cat.report.internals;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportFilter;
import org.unidal.cat.report.ReportPeriod;
import org.unidal.cat.report.spi.ReportDelegate;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportProvider.class, value = HistoricalReportProvider.ID)
public class HistoricalReportProvider<T extends Report> implements ReportProvider<T> {
	public static final String ID = "historical";

	@Inject
	private ReportStorage<T> m_storage;

	@Override
	public boolean isEligible(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain) {
		return !period.isHistorical(startTime);
	}

	@Override
	public T getReport(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain,
	      ReportFilter<T> filter) throws IOException {
		List<T> reports = m_storage.loadAll(delegate, period, startTime, domain);

		if (reports.isEmpty()) {
			return null;
		} else {
			T aggregated = delegate.aggregate(period, reports);

			if (filter != null) {
				filter.applyTo(aggregated);
			}

			return aggregated;
		}
	}
}

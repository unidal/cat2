package org.unidal.cat.report.internals;

import java.io.IOException;
import java.util.List;

import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportFilter;
import org.unidal.cat.report.spi.ReportDelegate;
import org.unidal.cat.report.spi.remote.RemoteContext;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportProvider.class, value = HistoricalReportProvider.ID)
public class HistoricalReportProvider<T extends Report> implements ReportProvider<T> {
	public static final String ID = "historical";

	@Inject
	private ReportStorage<T> m_storage;

	@Override
	public boolean isEligible(RemoteContext ctx, ReportDelegate<T> delegate) {
		return ctx.getPeriod().isHistorical(ctx.getStartTime());
	}

	@Override
	public T getReport(RemoteContext ctx, ReportDelegate<T> delegate) throws IOException {
		List<T> reports = m_storage.loadAll(delegate, ctx.getPeriod(), ctx.getStartTime(), ctx.getDomain());

		if (reports.isEmpty()) {
			return null;
		} else {
			T aggregated = delegate.aggregate(ctx.getPeriod(), reports);
			ReportFilter<T> filter = ctx.getFilter();

			if (filter != null) {
				filter.applyTo(ctx, aggregated);
			}

			return aggregated;
		}
	}
}

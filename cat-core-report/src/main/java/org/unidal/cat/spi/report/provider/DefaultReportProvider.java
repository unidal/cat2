package org.unidal.cat.spi.report.provider;

import java.io.IOException;

import org.unidal.cat.core.report.remote.RemoteReportContext;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportProvider.class)
public class DefaultReportProvider<T extends Report> implements ReportProvider<T> {
	@Inject(RecentReportProvider.ID)
	private ReportProvider<T> m_recent;

	@Inject(HistoricalReportProvider.ID)
	private ReportProvider<T> m_historical;

	@Override
	public boolean isEligible(RemoteReportContext ctx, ReportDelegate<T> delegate) {
		return true;
	}

	@Override
	public T getReport(RemoteReportContext ctx, ReportDelegate<T> delegate) throws IOException {
		if (m_historical.isEligible(ctx, delegate)) {
			return m_historical.getReport(ctx, delegate);
		} else {
			return m_recent.getReport(ctx, delegate);
		}
	}
}

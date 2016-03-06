package org.unidal.cat.spi.report.provider;

import java.io.IOException;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportProvider.class)
public class DefaultReportProvider<T extends Report> implements ReportProvider<T> {
	@Inject(RecentReportProvider.ID)
	private ReportProvider<T> m_rencent;

	@Inject(HistoricalReportProvider.ID)
	private ReportProvider<T> m_historical;

	@Override
	public boolean isEligible(RemoteContext ctx, ReportDelegate<T> delegate) {
		return true;
	}

	@Override
	public T getReport(RemoteContext ctx, ReportDelegate<T> delegate) throws IOException {
		if (m_historical.isEligible(ctx, delegate)) {
			return m_historical.getReport(ctx, delegate);
		} else {
			return m_rencent.getReport(ctx, delegate);
		}
	}
}

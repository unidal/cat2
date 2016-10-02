package org.unidal.cat.spi.report.provider;

import java.io.IOException;

import org.unidal.cat.core.report.remote.RemoteReportContext;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.report.ReportDelegate;

public interface ReportProvider<T extends Report> {
	public boolean isEligible(RemoteReportContext ctx, ReportDelegate<T> delegate);

	public T getReport(RemoteReportContext ctx, ReportDelegate<T> delegate) throws IOException;
}

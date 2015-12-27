package org.unidal.cat.report.internals;

import java.io.IOException;

import org.unidal.cat.report.Report;
import org.unidal.cat.report.spi.ReportDelegate;
import org.unidal.cat.report.spi.remote.RemoteContext;

public interface ReportProvider<T extends Report> {
	public boolean isEligible(RemoteContext ctx, ReportDelegate<T> delegate);

	public T getReport(RemoteContext ctx, ReportDelegate<T> delegate) throws IOException;
}

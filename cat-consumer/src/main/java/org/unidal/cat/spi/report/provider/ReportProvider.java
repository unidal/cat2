package org.unidal.cat.spi.report.provider;

import java.io.IOException;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.remote.RemoteContext;
import org.unidal.cat.spi.report.ReportDelegate;

public interface ReportProvider<T extends Report> {
	public boolean isEligible(RemoteContext ctx, ReportDelegate<T> delegate);

	public T getReport(RemoteContext ctx, ReportDelegate<T> delegate) throws IOException;
}

package org.unidal.cat.report;

import org.unidal.cat.report.spi.remote.RemoteContext;

public interface ReportFilter<T extends Report> {
	public String getReportName();

	public String getId();

	public void applyTo(T report);

	public void applyTo(RemoteContext ctx, T report);
}

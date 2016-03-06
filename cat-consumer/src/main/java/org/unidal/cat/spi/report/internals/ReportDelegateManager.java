package org.unidal.cat.spi.report.internals;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.report.ReportDelegate;

public interface ReportDelegateManager {
	public <T extends Report> ReportDelegate<T> getDelegate(String reportName);
}

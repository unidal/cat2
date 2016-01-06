package org.unidal.cat.report.spi;

import org.unidal.cat.report.Report;

public interface ReportDelegateManager {
	public <T extends Report> ReportDelegate<T> getDelegate(String reportName);
}

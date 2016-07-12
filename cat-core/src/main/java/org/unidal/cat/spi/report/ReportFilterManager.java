package org.unidal.cat.spi.report;

import org.unidal.cat.spi.Report;

public interface ReportFilterManager {
	public <T extends Report> ReportFilter<T> getFilter(String reportName, String id);
}

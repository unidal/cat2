package org.unidal.cat.spi;

import org.unidal.cat.spi.report.ReportFilter;

public interface ReportFilterManager {
	public <T extends Report> ReportFilter<T> getFilter(String reportName, String id);
}

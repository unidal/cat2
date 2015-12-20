package org.unidal.cat.report;

public interface ReportFilterManager {
	public <T extends Report> ReportFilter<T> getFilter(String reportName, String id);
}

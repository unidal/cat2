package org.unidal.cat.spi.report;

import org.unidal.cat.spi.Report;

public interface ReportReducerManager {
	public <T extends Report> ReportReducer<T> getReducer(String reportName, String id);
}

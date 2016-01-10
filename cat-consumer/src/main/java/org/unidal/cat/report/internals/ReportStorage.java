package org.unidal.cat.report.internals;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportPeriod;
import org.unidal.cat.report.spi.ReportDelegate;

public interface ReportStorage<T extends Report> {
	public void store(ReportDelegate<T> delegate, ReportPeriod period, T report, int index, ReportStoragePolicy policy)
	      throws IOException;

	public List<T> loadAll(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain)
	      throws IOException;
}

package org.unidal.cat.report.internals;

import java.io.IOException;
import java.util.Date;

import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportFilter;
import org.unidal.cat.report.ReportPeriod;
import org.unidal.cat.report.spi.ReportDelegate;

public interface ReportProvider<T extends Report> {
	public boolean isEligible(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain);

	public T getReport(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain,
	      ReportFilter<T> filter) throws IOException;
}

package org.unidal.cat.spi.report;

import org.unidal.cat.core.report.remote.RemoteReportContext;
import org.unidal.cat.spi.Report;

public interface ReportFilter<T extends Report> {
	/**
	 * returns report name.
	 * 
	 * @return report name
	 */
	public String getReportName();

	/**
	 * returns filter id of the report.
	 * 
	 * @return filter id of the report
	 */
	public String getId();

	/**
	 * Makes a new report from the given report to satisfy the context.
	 * 
	 * @param ctx
	 *           remote context
	 * @param report
	 *           the report should keep unchanged
	 * @return new created report
	 */
	public T screen(RemoteReportContext ctx, T report);

	/**
	 * Tailors the given report to satisfy the context.
	 * 
	 * @param ctx
	 *           remote context
	 * @param report
	 *           the report should be changed
	 */
	public void tailor(RemoteReportContext ctx, T report);
}

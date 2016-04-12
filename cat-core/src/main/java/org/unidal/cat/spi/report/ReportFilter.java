package org.unidal.cat.spi.report;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.remote.RemoteContext;

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
	public T screen(RemoteContext ctx, T report);

	/**
	 * Tailors the given report to satisfy the context.
	 * 
	 * @param ctx
	 *           remote context
	 * @param report
	 *           the report should be changed
	 */
	public void tailor(RemoteContext ctx, T report);
}

package org.unidal.cat.spi.report;

import java.util.List;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportPeriod;

/**
 * Make long period report from short period reports.
 * <p>
 * 
 * <ul>
 * <li>Daily report from hour reports</li>
 * <li>Weekly report from daily reports</li>
 * <li>Monthly report from daily reports</li>
 * <li>Annual report from monthly reports</li>
 * </ul>
 *
 * @param <T>
 */
public interface ReportReducer<T extends Report> {
	/**
	 * @return reducer id of the report
	 */
	public String getId();

	/**
	 * @return report period
	 */
	public ReportPeriod getPeriod();

	/**
	 * @return report name
	 */
	public String getReportName();

	/**
	 * Make longer period report from a list of shorter period reports.
	 * @param reports
	 *           list of reports, which should be kept unchanged
	 * 
	 * @return new created report
	 */
	public T reduce(List<T> reports);
}

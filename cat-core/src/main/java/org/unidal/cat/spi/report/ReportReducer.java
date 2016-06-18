package org.unidal.cat.spi.report;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.remote.RemoteContext;

import com.dianping.cat.message.Transaction;

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
	 * 
	 * @param ctx
	 *           reduce context
	 * @param reports
	 *           list of reports, which should be kept unchanged
	 * @return new created report
	 */
	public T reduce(Context ctx, List<T> reports);

	public interface Context {
		public String getDomain();

		public <T extends Report> ReportFilter<T> getFilter();

		public int getIntProperty(String string, int i);

		public String getName();

		public Map<String, String> getProperties();

		public String getProperty(String property, String defaultValue);

		public Date getStartTime();

		public void setParentTransaction(Transaction parent);

		public RemoteContext setProperty(String property, String newValue);

	}
}

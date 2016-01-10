package org.unidal.cat.report.spi.remote;

import java.util.Date;
import java.util.Map;

import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportFilter;
import org.unidal.cat.report.ReportPeriod;

import com.dianping.cat.message.Transaction;

public interface RemoteContext {
	public String buildURL(String serverUriPrefix);

	public void destroy();

	public String getDomain();

	public <T extends Report> ReportFilter<T> getFilter();

	public int getIntProperty(String string, int i);

	public String getName();

	public Transaction getParentTransaction();

	public ReportPeriod getPeriod();

	public Map<String, String> getProperties();

	public String getProperty(String property, String defaultValue);

	public Date getStartTime();

	public void setParentTransaction(Transaction parent);

	public RemoteContext setProperty(String property, String newValue);

}

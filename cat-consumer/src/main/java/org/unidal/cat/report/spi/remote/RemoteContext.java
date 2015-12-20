package org.unidal.cat.report.spi.remote;

import java.util.Date;
import java.util.Map;

import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportFilter;
import org.unidal.cat.report.ReportPeriod;

public interface RemoteContext {
	public String buildURL(String serverUriPrefix);

	public String getDomain();

	public <T extends Report> ReportFilter<T> getFilter();

	public String getName();

	public ReportPeriod getPeriod();

	public Map<String, String> getProperties();

	public String getProperty(String property, String defaultValue);

	public int getIntProperty(String string, int i);

	public Date getStartTime();

	public void setProperty(String property, String newValue);

}

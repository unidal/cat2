package org.unidal.cat.report;

import java.util.Date;

public interface Report {
	public String getDomain();

	public Date getEndTime();

	public ReportPeriod getPeriod();

	public Date getStartTime();
}

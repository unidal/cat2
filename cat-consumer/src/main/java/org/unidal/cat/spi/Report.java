package org.unidal.cat.spi;

import java.util.Date;

public interface Report {
	public String getDomain();

	public Date getEndTime();

	public ReportPeriod getPeriod();

	public Date getStartTime();
}

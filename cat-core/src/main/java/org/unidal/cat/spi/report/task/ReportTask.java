package org.unidal.cat.spi.report.task;

import java.util.Date;
import java.util.List;

import org.unidal.cat.spi.ReportPeriod;

public interface ReportTask {
	public void done(String domain);

	public List<String> getDomains();

	public String getReportName();

	public ReportPeriod getSourcePeriod();

	public Date getSourceStartTime();

	public ReportPeriod getTargetPeriod();

}

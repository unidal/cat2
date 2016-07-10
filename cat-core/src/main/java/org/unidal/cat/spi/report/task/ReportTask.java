package org.unidal.cat.spi.report.task;

import java.util.Date;

import org.unidal.cat.spi.ReportPeriod;

public interface ReportTask {
	public int getId();

	public String getReportName();

	public ReportPeriod getSourcePeriod();

	public Date getTargetStartTime();

	public ReportPeriod getTargetPeriod();

	public int getFailureCount();

}

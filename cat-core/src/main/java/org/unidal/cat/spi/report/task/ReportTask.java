package org.unidal.cat.spi.report.task;

import java.util.Date;

import org.unidal.cat.spi.ReportPeriod;

public interface ReportTask {
	public int getFailureCount();

	public int getId();

	public String getReportName();

	public ReportPeriod getSourcePeriod();

	public Date getTargetEndTime();

	public ReportPeriod getTargetPeriod();

	public Date getTargetStartTime();

}

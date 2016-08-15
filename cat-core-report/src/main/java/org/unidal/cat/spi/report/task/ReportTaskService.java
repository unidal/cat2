package org.unidal.cat.spi.report.task;

import java.util.Date;

import org.unidal.cat.spi.ReportPeriod;

public interface ReportTaskService {
	public void add(String id, ReportPeriod targetPeriod, Date startTime, String reportName, Date scheduleTime) throws Exception;

	public void complete(ReportTask task) throws Exception;

	public void fail(ReportTask task, String reason) throws Exception;

	public ReportTask pull(String id) throws Exception;
}

package org.unidal.cat.spi.report.task;

import java.util.Date;

public interface ReportTaskService {
	public void add(String type, String report, Date scheduleDate) throws Exception;

	public void complete(ReportTask task) throws Exception;

	public void fail(ReportTask task, String reason) throws Exception;

	public ReportTask pull(String id) throws Exception;
}

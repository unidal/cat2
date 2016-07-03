package org.unidal.cat.spi.report.task;

import java.util.Date;

public interface ReportTaskService {
	public void add(String type, String report, Date scheduleDate);

	public void complete(ReportTask task);

	public void fail(ReportTask task, String reason);

	public ReportTask pull(String id);
}

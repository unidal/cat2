package org.unidal.cat.spi.report.task.internals;

import java.util.Date;

import org.unidal.cat.spi.report.task.ReportTask;
import org.unidal.cat.spi.report.task.ReportTaskService;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportTaskService.class)
public class DefaultReportTaskService implements ReportTaskService {
	@Override
	public void add(String type, String report, Date scheduleDate) {

	}

	@Override
	public void complete(ReportTask task) {

	}

	@Override
	public void fail(ReportTask task, String reason) {

	}

	@Override
	public ReportTask pull(String id) {
		return null;
	}
}

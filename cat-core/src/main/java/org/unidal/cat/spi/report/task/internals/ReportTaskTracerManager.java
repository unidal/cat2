package org.unidal.cat.spi.report.task.internals;

import java.io.IOException;

import org.unidal.cat.spi.report.task.ReportTask;

public interface ReportTaskTracerManager {
	public ReportTaskTracker open(ReportTask task) throws IOException;

	public void close(ReportTaskTracker tracker) throws IOException;
}

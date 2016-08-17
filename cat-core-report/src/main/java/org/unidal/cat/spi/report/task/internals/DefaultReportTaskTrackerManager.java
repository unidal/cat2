package org.unidal.cat.spi.report.task.internals;

import java.io.IOException;

import org.unidal.cat.spi.report.task.ReportTask;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportTaskTracerManager.class)
public class DefaultReportTaskTrackerManager extends ContainerHolder implements ReportTaskTracerManager {
	@Override
	public ReportTaskTracker open(ReportTask task) throws IOException {
		ReportTaskTracker tracker = lookup(ReportTaskTracker.class);

		tracker.open(task);
		return tracker;
	}

	@Override
	public void close(ReportTaskTracker tracker) throws IOException {
		release(tracker); // to avoid memory leak
		tracker.close();
	}
}

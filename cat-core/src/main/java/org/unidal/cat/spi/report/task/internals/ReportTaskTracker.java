package org.unidal.cat.spi.report.task.internals;

import java.io.IOException;
import java.util.List;

import org.unidal.cat.spi.report.task.ReportTask;

public interface ReportTaskTracker {
	public void close() throws IOException;

	public void done(String domain) throws IOException;

	public List<String> getDomains();

	public void open(ReportTask task) throws IOException;
}

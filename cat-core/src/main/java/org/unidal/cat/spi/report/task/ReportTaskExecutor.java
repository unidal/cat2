package org.unidal.cat.spi.report.task;

import java.io.IOException;

public interface ReportTaskExecutor {
	public void execute(ReportTask task) throws IOException;
}

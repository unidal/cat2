package org.unidal.cat.core.report.remote;

import java.io.IOException;
import java.io.OutputStream;

public interface RemoteReportSkeleton {
	public void handleReport(RemoteReportContext ctx, OutputStream out) throws IOException;
}

package org.unidal.cat.core.report.remote;

import java.io.IOException;
import java.io.InputStream;

public interface RemoteReportStub {
	public InputStream getReport(RemoteReportContext ctx, String server) throws IOException;
}

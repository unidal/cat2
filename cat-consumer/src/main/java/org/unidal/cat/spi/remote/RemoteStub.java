package org.unidal.cat.spi.remote;

import java.io.IOException;
import java.io.InputStream;

public interface RemoteStub {
	public InputStream getReport(RemoteContext ctx, String server) throws IOException;
}

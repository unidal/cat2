package org.unidal.cat.spi.remote;

import java.io.IOException;
import java.io.OutputStream;

public interface RemoteSkeleton {
	public void handleReport(RemoteContext ctx, OutputStream out) throws IOException;
}

package org.unidal.cat.spi;

import java.io.File;
import java.util.Map;

public interface ReportConfiguration {
	public int getAnanlyzerCount(String name);

	public File getBaseDataDir();

	public int getRemoteCallConnectTimeoutInMillis();

	public int getRemoteCallReadTimeoutInMillis();

	public int getRemoteCallThreads();

	public Map<String, Boolean> getServers();

	public String getServerUriPrefix(String server);
	
	public boolean isLocalMode();

	public void setBaseDataDir(File baseDataDir);

	public int getCheckpointThreads();
}
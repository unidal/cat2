package org.unidal.cat.spi;

import java.io.File;
import java.util.Map;

public interface ReportConfiguration {

	public int getRemoteCallThreads();

	public int getRemoteCallConnectTimeoutInMillis();

	public int getRemoteCallReadTimeoutInMillis();

	public String getServerUriPrefix(String server);

	public Map<String, Boolean> getServers();

	public boolean isLocalMode();

	public File getBaseDataDir();
	
	public void setBaseDataDir(File baseDataDir);

}
package org.unidal.cat.report;

import java.io.File;
import java.util.Map;

public interface ReportConfiguration {

	public int getRemoteCallThreads();

	public int getRemoteCallTimeoutInMillis();

	public String getServerUriPrefix(String server);

	public Map<String, Boolean> getServers();

	public boolean isLocalMode();

	public File getBaseDataDir();

}
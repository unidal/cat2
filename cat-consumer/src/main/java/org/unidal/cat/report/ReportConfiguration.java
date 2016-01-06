package org.unidal.cat.report;

import java.util.Map;

public interface ReportConfiguration {

	public abstract int getRemoteCallThreads();

	public abstract int getRemoteCallTimeoutInMillis();

	public abstract String getServerUriPrefix(String server);

	public abstract Map<String, Boolean> getServers();

}
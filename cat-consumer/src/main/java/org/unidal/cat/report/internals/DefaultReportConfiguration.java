package org.unidal.cat.report.internals;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.report.ReportConfiguration;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportConfiguration.class)
public class DefaultReportConfiguration implements Initializable, ReportConfiguration {
	private ConcurrentMap<String, Boolean> m_servers = new ConcurrentHashMap<String, Boolean>();

	private String m_serverUriPrefixPattern = "http://%s/cat/r/service";

	@Override
	public int getRemoteCallThreads() {
		return 20;
	}

	@Override
	public int getRemoteCallTimeoutInMillis() {
		return 10 * 1000     * 60;
	}

	@Override
	public String getServerUriPrefix(String server) {
		return String.format(m_serverUriPrefixPattern, server);
	}

	@Override
	public Map<String, Boolean> getServers() {
		return m_servers;
	}

	@Override
	public void initialize() throws InitializationException {
		m_servers.putIfAbsent("127.0.0.1:2281", true);
	}
}

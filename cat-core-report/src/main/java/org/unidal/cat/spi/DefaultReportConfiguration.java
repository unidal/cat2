package org.unidal.cat.spi;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportConfiguration.class)
public class DefaultReportConfiguration implements Initializable, ReportConfiguration {
	private ConcurrentMap<String, Boolean> m_servers = new ConcurrentHashMap<String, Boolean>();

	private String m_serverUriPrefixPattern = "http://%s/cat/r/service";

	private File m_baseDataDir;

	@Override
	public int getAnanlyzerCount(String name) {
		return 2; // TODO
	}

	@Override
	public File getBaseDataDir() {
		return m_baseDataDir;
	}

	@Override
	public int getRemoteCallConnectTimeoutInMillis() {
		return 1 * 1000; // 1s
	}

	@Override
	public int getRemoteCallReadTimeoutInMillis() {
		return 10 * 1000; // 10s
	}

	@Override
	public int getRemoteCallThreads() {
		return 20;
	}

	@Override
	public Map<String, Boolean> getServers() {
		return m_servers;
	}

	@Override
	public String getServerUriPrefix(String server) {
		return String.format(m_serverUriPrefixPattern, server);
	}

	@Override
	public void initialize() throws InitializationException {
		m_baseDataDir = new File("/data/appdatas/cat/bucket");
		m_servers.putIfAbsent("127.0.0.1:2281", true);
	}

	@Override
	public boolean isLocalMode() {
		return false;
	}

	@Override
	public void setBaseDataDir(File baseDataDir) {
		m_baseDataDir = baseDataDir;
	}

	@Override
	public int getCheckpointThreads() {
		return 3;
	}
}

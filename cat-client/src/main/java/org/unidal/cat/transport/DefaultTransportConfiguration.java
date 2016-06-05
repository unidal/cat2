package org.unidal.cat.transport;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Named;

@Named(type = TransportConfiguration.class)
public class DefaultTransportConfiugration implements TransportConfiguration, Initializable {
	private int m_bossThreads;

	private int m_workerThreads;

	@Override
	public int getBossThreads() {
		return m_bossThreads;
	}

	@Override
	public int getWorkerThreads() {
		return m_workerThreads;
	}

	@Override
	public int getTcpPort() {
		// 2280 comes from cellphone pad, C:2, A:2, T:8
		return 2280;
	}

	@Override
	public void initialize() throws InitializationException {
		m_bossThreads = Runtime.getRuntime().availableProcessors(); // TODO tune it
		m_workerThreads = Runtime.getRuntime().availableProcessors(); // TODO tune it
	}
}

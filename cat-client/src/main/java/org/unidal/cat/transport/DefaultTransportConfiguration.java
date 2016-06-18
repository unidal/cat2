package org.unidal.cat.transport;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.net.SocketAddressProvider;

@Named(type = TransportConfiguration.class)
public class DefaultTransportConfiguration implements TransportConfiguration, Initializable {
	@Inject
	private SocketAddressProvider m_addressProvider;

	private int m_threads;

	@Override
	public SocketAddressProvider getAddressProvider() {
		return m_addressProvider;
	}

	@Override
	public int getThreads() {
		return m_threads;
	}

	@Override
	public void initialize() throws InitializationException {
		m_threads = Runtime.getRuntime().availableProcessors(); // TODO tune it
	}
}

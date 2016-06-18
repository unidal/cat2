package org.unidal.cat.transport;

import org.unidal.net.SocketAddressProvider;

public interface TransportConfiguration {
	public SocketAddressProvider getAddressProvider();

	public int getThreads();
}

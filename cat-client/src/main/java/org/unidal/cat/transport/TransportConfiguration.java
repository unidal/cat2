package org.unidal.cat.transport;

public interface TransportConfiguration {
	public int getBossThreads();

	public int getWorkerThreads();

	public int getTcpPort();
}

package org.unidal.cat.spi.transport;

public interface ServerTransportConfiguration {
	public int getBossThreads();

	public int getWorkerThreads();

	public int getTcpPort();
}

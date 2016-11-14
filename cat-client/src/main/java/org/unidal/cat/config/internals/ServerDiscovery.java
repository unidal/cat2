package org.unidal.cat.config.internals;

import java.net.InetSocketAddress;
import java.util.List;

public interface ServerDiscovery {
   public List<InetSocketAddress> getMetaServers();
}

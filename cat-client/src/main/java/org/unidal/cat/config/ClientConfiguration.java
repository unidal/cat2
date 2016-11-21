package org.unidal.cat.config;

import java.net.InetSocketAddress;
import java.util.List;

public interface ClientConfiguration {
   public boolean isEnabled();

   public List<InetSocketAddress> getServersForTree();
}

package org.unidal.cat.config;

import java.net.InetSocketAddress;
import java.util.List;

public interface ClientConfiguration {
   public static String TYPE_LOGTREE = "logtree";

   public boolean isEnabled();

   public List<InetSocketAddress> getServerNodes(String type);
}

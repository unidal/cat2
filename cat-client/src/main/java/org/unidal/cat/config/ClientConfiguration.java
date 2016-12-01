package org.unidal.cat.config;

import java.net.InetSocketAddress;
import java.util.List;

public interface ClientConfiguration {
   public String getDomain();

   public int getMaxMessageLines();

   public long getRefreshInterval();

   public String getServerConfigUrl();

   public List<String> getServersForPlugin();

   public List<InetSocketAddress> getServersForTree();

   public int getTaggedTransactionCacheSize();

   public boolean isDumpLockedThread();

   public boolean isEnabled();
}

package org.unidal.cat.config;

import java.net.InetSocketAddress;
import java.util.List;

import com.dianping.cat.message.spi.MessageTree;

public interface ClientConfiguration {
   public String getDomain();

   public int getMaxMessageLines();

   public long getRefreshInterval();

   public double getSampleRatio();

   public String getServerConfigUrl();

   public List<String> getServersForPlugin();

   public List<InetSocketAddress> getServersForTree();

   public int getTaggedTransactionCacheSize();

   public boolean isBlocked();

   public boolean isDumpLockedThread();

   public boolean isEnabled();

   public boolean isAtomic(MessageTree tree);
}

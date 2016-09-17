package org.unidal.cat.core.message.config;

import java.util.Map;

public interface MessageConfiguration {
   public int getHdfsMaxStorageTime();

   public boolean isUseHdfs();

   public Map<String, Boolean> getServers();

   public int getRemoteCallReadTimeoutInMillis();

   public String getServerUriPrefix(String server);

   public int getRemoteCallConnectTimeoutInMillis();

   public int getRemoteCallThreads();
}

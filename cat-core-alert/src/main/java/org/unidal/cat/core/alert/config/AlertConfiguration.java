package org.unidal.cat.core.alert.config;

import java.util.Map;

public interface AlertConfiguration {
   public long getAlertCheckInterval();

   public int getRemoteCallConnectTimeoutInMillis();

   public int getRemoteCallReadTimeoutInMillis();

   public int getRemoteCallThreads();

   public Map<String, Boolean> getServers();

   public String getServerUri(String server);

   public boolean isEnabled();
}
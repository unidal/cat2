package org.unidal.cat.core.alert;

import java.util.Map;

public interface AlertConfiguration {
   public int getRemoteCallConnectTimeoutInMillis();

   public int getRemoteCallReadTimeoutInMillis();

   public int getRemoteCallThreads();

   public Map<String, Boolean> getServers();

   public String getServerUri(String server);

   public long getAlertCheckInterval();
}
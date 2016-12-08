package org.unidal.cat.config;

import com.dianping.cat.configuration.client.entity.ClientConfig;

public interface ClientEnvironmentSettings {
   public String getCatHome();

   public ClientConfig getClientXml();

   public String getDefaultCatServer();

   public int getDefaultCatServerPort();

   public String getDomain();

   public String getHostName();

   public String getIpAddress();

   public String getRemoteConfigUrlPattern();

   public boolean isTestMode();

   public boolean isServerMode();
}

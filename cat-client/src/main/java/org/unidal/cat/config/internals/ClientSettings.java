package org.unidal.cat.config.internals;

import com.dianping.cat.configuration.client.entity.ClientConfig;

public interface ClientSettings {
   public String getCatHome();

   public ClientConfig getClientXml();

   public String getDefaultCatServer();

   public int getDefaultCatServerPort();

   public String getDomain();

   public String getHostName();

   public String getIpAddress();

   public String getRemoteConfigUrlPattern();
}

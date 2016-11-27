package org.unidal.cat.config.internals;

import java.io.File;

public interface ClientSettings {
   public String getCatHome();

   public File getClientXmlFile();

   public String getDefaultCatServer();

   public int getDefaultCatServerPort();

   public String getDomain();

   public String getHostName();

   public String getIpAddress();

   public String getRemoteConfigUrlPattern();
}

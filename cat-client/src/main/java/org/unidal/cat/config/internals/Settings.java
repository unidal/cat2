package org.unidal.cat.config.internals;

import java.io.File;

public interface Settings {
   public String getCatDataDir();

   public String getCatHome();

   public String getCatLogsDir();

   public File getClientXmlFile();

   public String getDefaultCatServer();

   public int getDefaultCatServerPort();

   public String getDomain();

   public String getRemoteConfigUrlPattern();

}

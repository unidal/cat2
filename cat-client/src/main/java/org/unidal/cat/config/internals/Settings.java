package org.unidal.cat.config.internals;

import java.io.File;

public interface Settings {
   public String getCatHome();

   public File getClientXmlFile();

   public int getDefaultServerHttpPort();

   public String getRemoteConfigUrlPattern();
}

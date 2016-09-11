package org.unidal.cat.core.config.spi;

public interface ConfigStore {
   public String getConfig();

   public void setConfig(String config);
}

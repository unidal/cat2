package org.unidal.cat.core.config.spi;

public interface ConfigStoreManager {
   public String GROUP_REPORT = "report";

   public ConfigStore getConfigStore(String group, String name);

   public void refresh(String group, String name);

   public void register(String group, String name, ConfigChangeCallback callback);
}

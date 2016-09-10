package org.unidal.cat.core.config.spi;

public interface ConfigStoreManager {
   public ConfigStore getConfigStore(String group, String name);

   public void refresh(String group, String name);

   public void register(String group, String name, ConfigChangeCallback callback);
}

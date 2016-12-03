package org.unidal.cat.core.config.spi;

public interface ConfigStoreManager {
   public ConfigStore getConfigStore(String group, String name);

   public void onChanged(String group, String name, String config) throws ConfigException;

   public void register(String group, String name, ConfigChangeListener callback);

   public void reloadConfigStore(String group, String name) throws ConfigException;
}

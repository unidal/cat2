package org.unidal.cat.core.config.spi;

public interface ConfigStoreGroup {
   public ConfigStore getConfigStore(String name);
}

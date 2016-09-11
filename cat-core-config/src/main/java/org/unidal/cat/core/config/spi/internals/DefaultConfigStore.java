package org.unidal.cat.core.config.spi.internals;

import org.unidal.cat.core.config.spi.ConfigStore;

public class DefaultConfigStore implements ConfigStore {
   private String m_config;

   public DefaultConfigStore(String config) {
      m_config = config;
   }

   @Override
   public String getConfig() {
      return m_config;
   }

   @Override
   public void setConfig(String config) {
      m_config = config;
   }
}

package org.unidal.cat.core.config.spi;

public interface ConfigChangeCallback {
   public void onConfigChange(String config) throws ConfigException;
}

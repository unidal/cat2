package org.unidal.cat.core.config.spi;

public interface ConfigChangeListener {
   /**
    * Applies the config change, throws excepiton if any error.
    * 
    * @param config
    *           to be applied
    * @exception ConfigException
    *               should be thrown if the config is NOT changeable
    */
   public void onChanged(String config) throws ConfigException;
}

package org.unidal.cat.core.message.config;

import org.unidal.lookup.annotation.Named;

@Named(type = MessageConfiguration.class)
public class DefaultMessageConfiguration implements MessageConfiguration {
   @Override
   public int getHdfsMaxStorageTime() {
      return 30;
   }
}

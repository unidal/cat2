package org.unidal.cat.core.config.spi.internals;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.unidal.cat.core.config.spi.ConfigChangeCallback;
import org.unidal.cat.core.config.spi.ConfigStore;
import org.unidal.cat.core.config.spi.ConfigStoreGroup;
import org.unidal.cat.core.config.spi.ConfigStoreManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = ConfigStoreManager.class)
public class DefaultConfigStoreManager extends ContainerHolder implements ConfigStoreManager {
   private Map<String, ConfigStore> m_stores = new HashMap<String, ConfigStore>();

   private Map<String, ConfigChangeCallback> m_callbacks = new LinkedHashMap<String, ConfigChangeCallback>();

   private ConfigStore createConfigStore(String key, String group, String name) {
      if (hasComponent(ConfigStore.class, key)) {
         return lookup(ConfigStore.class, key);
      } else if (hasComponent(ConfigStoreGroup.class, group)) {
         ConfigStoreGroup g = lookup(ConfigStoreGroup.class, group);

         return g.getConfigStore(name);
      }

      return null;
   }

   @Override
   public ConfigStore getConfigStore(String group, String name) {
      String key = group + ":" + name;
      ConfigStore store = m_stores.get(key);

      if (store == null) {
         store = createConfigStore(key, group, name);

         if (store == null) {
            store = new DefaultConfigStore(null);
         }

         m_stores.put(key, store);
      }

      return store;
   }

   @Override
   public void refresh(String group, String name) {
      String key = group + ":" + name;
      ConfigChangeCallback callback = m_callbacks.get(key);

      if (callback != null) {
         ConfigStore store = createConfigStore(key, group, name);

         if (store != null) {
            ConfigStore existed = m_stores.get(key);

            if (existed != null) {
               existed.setConfig(store.getConfig());
            } else {
               m_stores.put(key, store);
            }

            callback.onConfigChange(store.getConfig());
         }
      }
   }

   @Override
   public void register(String group, String name, ConfigChangeCallback callback) {
      String key = group + ":" + name;

      if (callback == null) {
         m_callbacks.remove(key);
      } else {
         m_callbacks.put(key, callback);
      }
   }
}

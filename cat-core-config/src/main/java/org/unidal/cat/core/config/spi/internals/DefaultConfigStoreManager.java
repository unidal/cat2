package org.unidal.cat.core.config.spi.internals;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.unidal.cat.core.config.spi.ConfigChangeListener;
import org.unidal.cat.core.config.spi.ConfigException;
import org.unidal.cat.core.config.spi.ConfigStore;
import org.unidal.cat.core.config.spi.ConfigStoreGroup;
import org.unidal.cat.core.config.spi.ConfigStoreManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = ConfigStoreManager.class)
public class DefaultConfigStoreManager extends ContainerHolder implements ConfigStoreManager {
   private Map<String, ConfigStore> m_stores = new HashMap<String, ConfigStore>();

   private Map<String, ConfigChangeListener> m_listeners = new LinkedHashMap<String, ConfigChangeListener>();

   private ConfigStore createConfigStore(String group, String name) {
      String key = group + ":" + name;

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
         store = createConfigStore(group, name);

         if (store == null) {
            store = new DefaultConfigStore(null);
         }

         m_stores.put(key, store);
      }

      return store;
   }

   @Override
   public void onChanged(String group, String name, String config) throws ConfigException {
      String key = group + ":" + name;
      ConfigChangeListener listener = m_listeners.get(key);

      if (listener != null) {
         // ConfigException should be thrown if the config is NOT appliable
         listener.onChanged(config);
      }

      ConfigStore existed = m_stores.get(key);

      if (existed != null) {
         existed.setConfig(config);
      }
   }

   @Override
   public void register(String group, String name, ConfigChangeListener callback) {
      String key = group + ":" + name;

      if (callback == null) {
         m_listeners.remove(key);
      } else {
         m_listeners.put(key, callback);
      }
   }

   @Override
   public void reloadConfigStore(String group, String name) throws ConfigException {
      String key = group + ":" + name;
      ConfigStore store = createConfigStore(group, name);
      ConfigChangeListener listener = m_listeners.get(key);

      if (listener != null) {
         // ConfigException would be thrown if the config is NOT appliable
         listener.onChanged(store.getConfig());
      }

      ConfigStore existed = m_stores.get(key);

      if (existed != null) {
         existed.setConfig(store.getConfig());
      } else {
         m_stores.put(key, store);
      }
   }
}

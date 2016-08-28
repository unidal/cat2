package org.unidal.cat.core.config;

import java.util.HashMap;
import java.util.Map;

import org.unidal.cat.core.config.dal.ReportConfig;
import org.unidal.cat.core.config.dal.ReportConfigDao;
import org.unidal.cat.core.config.dal.ReportConfigEntity;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ConfigProviderManager.class)
public class DefaultConfigProviderManager extends ContainerHolder implements ConfigProviderManager {
   @Inject
   private ReportConfigDao m_dao;

   private Map<String, ConfigProvider> m_cached = new HashMap<String, ConfigProvider>();

   @Override
   public synchronized ConfigProvider getConfigProvider(String name) {
      ConfigProvider provider = m_cached.get(name);

      // try to lookup
      if (provider == null && super.hasComponent(ConfigProvider.class, name)) {
         provider = super.lookup(ConfigProvider.class, name);
         m_cached.put(name, provider);
      }

      // try to load from MySQL
      if (provider == null) {
         try {
            ReportConfig config = m_dao.findByReportName(name, ReportConfigEntity.READSET_FULL);

            provider = new Provider(config.getContent());
         } catch (Throwable e) {
            // no configure at all
            provider = new Provider(null);
         }

         m_cached.put(name, provider);
      }

      return provider;
   }

   static class Provider implements ConfigProvider {
      private String m_config;

      public Provider(String config) {
         m_config = config;
      }

      @Override
      public String getConfig() {
         return m_config;
      }
   }
}

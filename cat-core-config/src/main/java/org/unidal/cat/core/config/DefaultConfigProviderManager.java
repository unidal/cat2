package org.unidal.cat.core.config;

import java.util.HashMap;
import java.util.Map;

import org.unidal.cat.core.config.dal.ReportConfig;
import org.unidal.cat.core.config.dal.ReportConfigDao;
import org.unidal.cat.core.config.dal.ReportConfigEntity;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
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

            provider = new Provider(name, config.getContent());
         } catch (Throwable e) {
            // no configure at all
            provider = new Provider(name, null);
         }

         m_cached.put(name, provider);
      }

      return provider;
   }

   class Provider implements ConfigProvider {
      private String m_name;

      private String m_config;

      public Provider(String name, String config) {
         m_name = name;
         m_config = config;
      }

      @Override
      public String getConfig() {
         return m_config;
      }

      @Override
      public void setConfig(String content) {
         try {
            ReportConfig c = m_dao.findByReportName(m_name, ReportConfigEntity.READSET_FULL);

            c.setContent(content);
            c.setVersion(c.getVersion() + 1);
            c.setFormat(1);
            c.setLastModifiedBy("Admin"); // TODO

            m_dao.updateByReportName(c, ReportConfigEntity.UPDATESET_FULL);
            m_config = content;
            return;
         } catch (DalNotFoundException e) {
            // continue
         } catch (DalException e) {
            throw new RuntimeException(String.format("Error when inserting report config(%s)!", m_name), e);
         }

         try {
            ReportConfig c = m_dao.createLocal();

            c.setReportName(m_name);
            c.setContent(content);
            c.setVersion(1);
            c.setFormat(1);
            c.setLastModifiedBy("Admin"); // TODO

            m_dao.insert(c);
            m_config = content;
         } catch (DalException e) {
            throw new RuntimeException(String.format("Error when updating report config(%s)!", m_name), e);
         }
      }
   }
}

package org.unidal.cat.core.config;

import org.unidal.cat.core.config.dal.SystemConfigDao;
import org.unidal.cat.core.config.dal.SystemConfigDo;
import org.unidal.cat.core.config.dal.SystemConfigEntity;
import org.unidal.cat.core.config.spi.ConfigStore;
import org.unidal.cat.core.config.spi.ConfigStoreGroup;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = ConfigStoreGroup.class, value = SystemConfigStoreGroup.ID)
public class SystemConfigStoreGroup implements ConfigStoreGroup {
   public static final String ID = "system";

   @Inject
   private SystemConfigDao m_dao;

   @Override
   public ConfigStore getConfigStore(String name) {
      return new SystemConfigStore(name);
   }

   private class SystemConfigStore implements ConfigStore {
      private String m_name;

      private String m_config;

      public SystemConfigStore(String name) {
         m_name = name;
      }

      @Override
      public String getConfig() {
         if (m_config == null) {
            try {
               SystemConfigDo c = m_dao.findByName(m_name, SystemConfigEntity.READSET_FULL);

               m_config = c.getContent();
            } catch (DalNotFoundException e) {
               Cat.logEvent("ConfigMissing", SystemConfigStoreGroup.ID + ":" + m_name);
            } catch (Throwable e) {
               Cat.logError(e);
            }
         }

         return m_config;
      }

      @Override
      public void setConfig(String config) {
         try {
            SystemConfigDo c = m_dao.findByName(m_name, SystemConfigEntity.READSET_FULL);

            c.setContent(config);
            c.setVersion(c.getVersion() + 1);
            c.setFormat(1);
            c.setLastModifiedBy("Admin"); // TODO

            m_dao.updateByName(c, SystemConfigEntity.UPDATESET_FULL);
            m_config = config;
            return;
         } catch (DalNotFoundException e) {
            // continue
         } catch (DalException e) {
            throw new RuntimeException(String.format("Error when updating report config(%s)!", m_name), e);
         }

         try {
            SystemConfigDo c = m_dao.createLocal();

            c.setName(m_name);
            c.setContent(config);
            c.setVersion(1);
            c.setFormat(1);
            c.setLastModifiedBy("Admin"); // TODO

            m_dao.insert(c);
            m_config = config;
         } catch (DalException e) {
            throw new RuntimeException(String.format("Error when inserting report config(%s)!", m_name), e);
         }
      }
   }
}

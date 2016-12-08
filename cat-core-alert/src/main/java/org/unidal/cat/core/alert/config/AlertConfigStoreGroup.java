package org.unidal.cat.core.alert.config;

import org.unidal.cat.core.alert.AlertConfigDao;
import org.unidal.cat.core.alert.AlertConfigDo;
import org.unidal.cat.core.alert.AlertConfigEntity;
import org.unidal.cat.core.config.spi.ConfigStore;
import org.unidal.cat.core.config.spi.ConfigStoreGroup;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = ConfigStoreGroup.class, value = AlertConfigStoreGroup.ID)
public class AlertConfigStoreGroup implements ConfigStoreGroup {
   public static final String ID = "alert";

   @Inject
   private AlertConfigDao m_dao;

   @Override
   public ConfigStore getConfigStore(String name) {
      return new AlertConfigStore(name);
   }

   private class AlertConfigStore implements ConfigStore {
      private String m_name;

      private String m_config;

      public AlertConfigStore(String name) {
         m_name = name;
      }

      @Override
      public String getConfig() {
         if (m_config == null) {
            try {
               AlertConfigDo c = m_dao.findByName(m_name, AlertConfigEntity.READSET_FULL);

               m_config = c.getContent();
            } catch (DalNotFoundException e) {
               Cat.logEvent("ConfigMissing", ID + ":" + m_name);
            } catch (Throwable e) {
               Cat.logError(e);
            }
         }

         return m_config;
      }

      @Override
      public void setConfig(String config) {
         try {
            AlertConfigDo c = m_dao.findByName(m_name, AlertConfigEntity.READSET_FULL);

            c.setContent(config);
            c.setVersion(c.getVersion() + 1);
            c.setFormat(1);
            c.setLastModifiedBy("Admin"); // TODO

            m_dao.updateByName(c, AlertConfigEntity.UPDATESET_FULL);
            m_config = config;
            return;
         } catch (DalNotFoundException e) {
            // continue
         } catch (DalException e) {
            throw new RuntimeException(String.format("Error when updating report config(%s)!", m_name), e);
         }

         try {
            AlertConfigDo c = m_dao.createLocal();

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

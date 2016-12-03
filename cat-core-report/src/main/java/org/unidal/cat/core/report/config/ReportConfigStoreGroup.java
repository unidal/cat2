package org.unidal.cat.core.report.config;

import org.unidal.cat.core.config.spi.ConfigStore;
import org.unidal.cat.core.config.spi.ConfigStoreGroup;
import org.unidal.cat.core.report.dal.ReportConfigDao;
import org.unidal.cat.core.report.dal.ReportConfigDo;
import org.unidal.cat.core.report.dal.ReportConfigEntity;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = ConfigStoreGroup.class, value = ReportConfigStoreGroup.ID)
public class ReportConfigStoreGroup implements ConfigStoreGroup {
   public static final String ID = "report";

   @Inject
   private ReportConfigDao m_dao;

   @Override
   public ConfigStore getConfigStore(String name) {
      return new ReportConfigStore(name);
   }

   private class ReportConfigStore implements ConfigStore {
      private String m_name;

      private String m_config;

      public ReportConfigStore(String name) {
         m_name = name;
      }

      @Override
      public String getConfig() {
         if (m_config == null) {
            try {
               ReportConfigDo c = m_dao.findByReportName(m_name, ReportConfigEntity.READSET_FULL);

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
            ReportConfigDo c = m_dao.findByReportName(m_name, ReportConfigEntity.READSET_FULL);

            c.setContent(config);
            c.setVersion(c.getVersion() + 1);
            c.setFormat(1);
            c.setLastModifiedBy("Admin"); // TODO

            m_dao.updateByReportName(c, ReportConfigEntity.UPDATESET_FULL);
            m_config = config;
            return;
         } catch (DalNotFoundException e) {
            // continue
         } catch (DalException e) {
            throw new RuntimeException(String.format("Error when updating report config(%s)!", m_name), e);
         }

         try {
            ReportConfigDo c = m_dao.createLocal();

            c.setReportName(m_name);
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

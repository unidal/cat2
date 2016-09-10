package org.unidal.cat.core.config.spi.internals;

import org.unidal.cat.core.config.dal.ReportConfig;
import org.unidal.cat.core.config.dal.ReportConfigDao;
import org.unidal.cat.core.config.dal.ReportConfigEntity;
import org.unidal.cat.core.config.spi.ConfigStore;
import org.unidal.cat.core.config.spi.ConfigStoreGroup;
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
      try {
         String key = ID + ":" + name;
         ReportConfig config = m_dao.findByReportName(key, ReportConfigEntity.READSET_FULL);

         return new DefaultConfigStore(config.getContent());
      } catch (Throwable e) {
         Cat.logError(e);
      }

      return null;
   }
}

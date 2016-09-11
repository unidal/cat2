package org.unidal.cat.core.config.service;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.config.domain.org.entity.DepartmentModel;
import org.unidal.cat.core.config.domain.org.entity.DomainOrgConfigModel;
import org.unidal.cat.core.config.domain.org.entity.ProductLineModel;
import org.unidal.cat.core.config.domain.org.entity.ProjectModel;
import org.unidal.cat.core.config.domain.org.transform.DefaultSaxParser;
import org.unidal.cat.core.config.spi.ConfigChangeListener;
import org.unidal.cat.core.config.spi.ConfigException;
import org.unidal.cat.core.config.spi.ConfigStoreManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = DomainOrgConfigService.class)
public class DefaultDomainOrgConfigService implements DomainOrgConfigService, ConfigChangeListener, Initializable {
   @Inject
   private ConfigStoreManager m_manager;

   private DomainOrgConfigModel m_config;

   @Override
   public String findDepartment(String domain) {
      if (domain != null && domain.length() > 0) {
         for (DepartmentModel department : m_config.getDepartments().values()) {
            for (ProductLineModel line : department.getProductLines().values()) {
               ProjectModel project = line.findProject(domain);

               if (project != null) {
                  return department.getId();
               }
            }
         }
      }

      return "Unknown";
   }

   @Override
   public DomainOrgConfigModel getConfig() {
      return m_config;
   }

   @Override
   public void initialize() throws InitializationException {
      String config = m_manager.getConfigStore("application", "domain.org").getConfig();

      if (config == null) {
         m_config = new DomainOrgConfigModel();
      } else {
         try {
            m_config = DefaultSaxParser.parse(config);
         } catch (Exception e) {
            throw new InitializationException("Unable to load config(application:domain.org)!", e);
         }
      }

      m_manager.register("application", "domain.org", this);
   }

   @Override
   public boolean isIn(String bu, String domain) {
      if (bu == null || domain == null) {
         return false;
      }

      DepartmentModel department = m_config.findDepartment(bu);

      if (department != null) {
         for (ProductLineModel line : department.getProductLines().values()) {
            ProjectModel project = line.findProject(domain);

            if (project != null) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public void onChanged(String config) throws ConfigException {
      try {
         m_config = DefaultSaxParser.parse(config);
      } catch (Exception e) {
         throw new ConfigException("Unable to parse config(application:domain.org)!", e);
      }
   }
}

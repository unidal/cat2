package org.unidal.cat.core.config;

import java.io.InputStream;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.config.domain.org.entity.DepartmentModel;
import org.unidal.cat.core.config.domain.org.entity.DomainOrgConfigModel;
import org.unidal.cat.core.config.domain.org.entity.ProductLineModel;
import org.unidal.cat.core.config.domain.org.entity.ProjectModel;
import org.unidal.cat.core.config.domain.org.transform.DefaultSaxParser;
import org.unidal.lookup.annotation.Named;

@Named(type = DomainOrgConfigService.class)
public class DefaultDomainOrgConfigService implements DomainOrgConfigService, Initializable {
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
      try {
         InputStream in = getClass().getResourceAsStream("domain-org-config.xml"); // TODO for test
         DomainOrgConfigModel config = DefaultSaxParser.parse(in);

         m_config = config;
      } catch (Exception e) {
         throw new InitializationException("Unable to load domain-org-config.xml!", e);
      }
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
}

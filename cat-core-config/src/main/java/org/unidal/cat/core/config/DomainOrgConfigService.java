package org.unidal.cat.core.config;

import org.unidal.cat.core.config.domain.org.entity.DomainOrgConfigModel;

public interface DomainOrgConfigService {
   public String findDepartment(String domain);

   public DomainOrgConfigModel getConfig();
}

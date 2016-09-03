package org.unidal.cat.core.config.service;

import org.unidal.cat.core.config.domain.org.entity.DomainOrgConfigModel;

public interface DomainOrgConfigService {
   public String findDepartment(String domain);

   public DomainOrgConfigModel getConfig();

   public boolean isIn(String bu, String domain);
}

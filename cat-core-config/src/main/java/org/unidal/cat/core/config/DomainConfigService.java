package org.unidal.cat.core.config;

import java.util.Set;

public interface DomainConfigService {
   public Set<String> getGroups(String domain);

   public boolean isInGroup(String domain, String group, String ip);
}

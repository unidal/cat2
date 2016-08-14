package org.unidal.cat.core.config;

import java.util.Set;

public interface DomainGroupConfigService {
   public Set<String> getGroups(String domain, Set<String> ips);

   public boolean isInGroup(String domain, String group, String ip);
}

package org.unidal.cat.core.report.nav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.unidal.cat.core.config.DomainConfigService;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = GroupBar.class, value = "domain", instantiationStrategy = Named.PER_LOOKUP)
public class DomainGroupBar implements GroupBar {
   @Inject
   private DomainConfigService m_configService;

   private String m_domain;

   private String m_group;

   private List<String> m_groups;

   private List<String> m_items;

   @Override
   public String getActiveGroup() {
      return m_group;
   }

   @Override
   public List<String> getActiveGroupItems() {
      return m_items;
   }

   @Override
   public List<String> getGroups() {
      return m_groups;
   }

   @Override
   public String getId() {
      return m_domain;
   }

   @Override
   public void initialize(String domain, String group, Set<String> ips) {
      m_domain = domain;
      m_group = group;
      m_groups = new ArrayList<String>(m_configService.getGroups(domain));
      m_items = new ArrayList<String>();

      for (String ip : ips) {
         if (m_configService.isInGroup(domain, group, ip)) {
            m_items.add(ip);
         }
      }

      Collections.sort(m_groups);
      Collections.sort(m_items);
   }
}

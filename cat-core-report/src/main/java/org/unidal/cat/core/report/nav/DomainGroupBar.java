package org.unidal.cat.core.report.nav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.unidal.cat.core.config.service.DomainGroupConfigService;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = DomainGroupBar.class, instantiationStrategy = Named.PER_LOOKUP)
public class DomainGroupBar implements GroupBar {
   @Inject
   private DomainGroupConfigService m_configService;

   private String m_domain;

   private String m_group;

   private List<String> m_groups;

   private List<String> m_items;

   private String m_activeItem;

   private String m_itemName;

   @Override
   public String getActiveGroup() {
      return m_group;
   }

   @Override
   public String getActiveGroupItem() {
      return m_activeItem;
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
   public String getItemName() {
      return m_itemName;
   }

   public void initialize(String domain, String group, String itemName, String activeItem, Set<String> items) {
      m_domain = domain;
      m_group = group;
      m_itemName = itemName;
      m_activeItem = activeItem;
      m_groups = new ArrayList<String>(m_configService.getGroups(domain, items));
      m_items = new ArrayList<String>();

      for (String item : items) {
         if (m_configService.isInGroup(domain, group, item)) {
            m_items.add(item);
         }
      }

      Collections.sort(m_groups);
      Collections.sort(m_items);
   }
}

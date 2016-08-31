package org.unidal.cat.core.config.service;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.config.domain.group.entity.DomainGroupConfigModel;
import org.unidal.cat.core.config.domain.group.entity.DomainModel;
import org.unidal.cat.core.config.domain.group.entity.MachineModel;
import org.unidal.cat.core.config.domain.group.transform.DefaultSaxParser;
import org.unidal.lookup.annotation.Named;

@Named(type = DomainGroupConfigService.class)
public class DefaultDomainGroupConfigService implements DomainGroupConfigService, Initializable {
   private DomainGroupConfigModel m_config;

   @Override
   public Set<String> getGroups(String domain, Set<String> ips) {
      Set<String> groups = new HashSet<String>();
      DomainModel d = m_config.findOrCreateDomain(domain);

      groups.add("All");

      if (d != null) {
         for (MachineModel m : d.getMachines().values()) {
            if (ips.contains(m.getIp())) {
               groups.addAll(m.getGroups());
            }
         }
      }

      return groups;
   }

   @Override
   public void initialize() throws InitializationException {
      try {
         InputStream in = getClass().getResourceAsStream("domain-group-config.xml");
         DomainGroupConfigModel config = DefaultSaxParser.parse(in);

         m_config = config;
      } catch (Exception e) {
         throw new InitializationException("Unable to load domain-group-config.xml!", e);
      }
   }

   @Override
   public boolean isInGroup(String domain, String group, String ip) {
      if (group == null) {
         return true;
      } else {
         DomainModel d = m_config.findDomain(domain);

         if (d != null) {
            MachineModel m = d.findMachine(ip);

            return m != null && m.getGroups().contains(group);
         } else {
            return false;
         }
      }
   }
}

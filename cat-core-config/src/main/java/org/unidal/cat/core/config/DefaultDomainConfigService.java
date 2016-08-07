package org.unidal.cat.core.config;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.config.domain.entity.DomainConfigModel;
import org.unidal.cat.core.config.domain.entity.DomainModel;
import org.unidal.cat.core.config.domain.entity.MachineModel;
import org.unidal.cat.core.config.domain.transform.DefaultSaxParser;
import org.unidal.lookup.annotation.Named;

@Named(type = DomainConfigService.class)
public class DefaultDomainConfigService implements DomainConfigService, Initializable {
   private DomainConfigModel m_config;

   @Override
   public Set<String> getGroups(String domain) {
      Set<String> groups = new HashSet<String>();
      DomainModel d = m_config.findOrCreateDomain(domain);

      groups.add("All");

      if (d != null) {
         for (MachineModel m : d.getMachines().values()) {
            groups.addAll(m.getGroups());
         }
      }

      return groups;
   }

   @Override
   public void initialize() throws InitializationException {
      try {
         InputStream in = getClass().getResourceAsStream("domain-config.xml");
         DomainConfigModel config = DefaultSaxParser.parse(in);

         m_config = config;
      } catch (Exception e) {
         throw new InitializationException("Unable to load domain-config.xml!", e);
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

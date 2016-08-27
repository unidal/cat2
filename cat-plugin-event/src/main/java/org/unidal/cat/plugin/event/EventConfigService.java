package org.unidal.cat.plugin.event;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.plugin.event.config.entity.EventConfigModel;
import org.unidal.cat.plugin.event.config.entity.IgnoreModel;
import org.unidal.cat.plugin.event.config.transform.DefaultSaxParser;
import org.unidal.lookup.annotation.Named;

@Named
public class EventConfigService implements Initializable {
   private Set<String> m_matchedDomains = new HashSet<String>();

   private List<String> m_startingDomains = new ArrayList<String>();

   public boolean isEligible(String domain) {
      if (m_matchedDomains.contains(domain)) {
         return false;
      }

      for (String startingDomain : m_startingDomains) {
         if (domain.startsWith(startingDomain)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void initialize() throws InitializationException {
      try {
         InputStream in = getClass().getResourceAsStream("config/event-config.xml"); // TODO for test
         EventConfigModel root = DefaultSaxParser.parse(in);

         initialize(root);
      } catch (Exception e) {
         throw new InitializationException("Unable to load event-config.xml!", e);
      }
   }

   private void initialize(EventConfigModel root) {
      for (IgnoreModel ignore : root.getIgnores()) {
         String domain = ignore.getDomain();

         if (domain.endsWith("*")) {
            String prefix = domain.substring(0, domain.length() - 1);

            if (!m_startingDomains.contains(prefix)) {
               m_startingDomains.add(prefix);
            }
         } else {
            m_matchedDomains.add(domain);
         }
      }
   }
}

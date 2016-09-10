package org.unidal.cat.plugin.transaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.config.ConfigProvider;
import org.unidal.cat.core.config.ConfigProviderManager;
import org.unidal.cat.plugin.transaction.config.entity.IgnoreModel;
import org.unidal.cat.plugin.transaction.config.entity.TransactionConfigModel;
import org.unidal.cat.plugin.transaction.config.transform.DefaultSaxParser;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named
public class TransactionConfigService implements Initializable {
   @Inject
   private ConfigProviderManager m_manager;

   private ConfigProvider m_configProvider;

   private Set<String> m_matchedDomains = new HashSet<String>();

   private List<String> m_startingDomains = new ArrayList<String>();

   public String getConfig() {
      return m_configProvider.getConfig();
   }

   @Override
   public void initialize() throws InitializationException {
      try {
         m_configProvider = m_manager.getConfigProvider(TransactionConstants.NAME);

         String xml = m_configProvider.getConfig();

         if (xml != null) {
            TransactionConfigModel root = DefaultSaxParser.parse(xml);

            initialize(root);
         }
      } catch (Exception e) {
         throw new InitializationException("Unable to load transaction config!", e);
      }
   }

   private void initialize(TransactionConfigModel root) {
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

   public void setConfig(String config) throws Exception {
      // validate
      DefaultSaxParser.parse(config);

      m_configProvider.setConfig(config);
   }
}

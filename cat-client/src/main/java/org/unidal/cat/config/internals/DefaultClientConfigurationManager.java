package org.unidal.cat.config.internals;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.config.ClientConfiguration;
import org.unidal.cat.config.ClientConfigurationManager;
import org.unidal.cat.config.ClientConfigurationProvider;
import org.unidal.cat.config.ClientEnvironmentSettings;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ClientConfigurationManager.class)
public class DefaultClientConfigurationManager implements ClientConfigurationManager, LogEnabled {
   @Inject
   private ClientEnvironmentSettings m_settings;

   @Inject(type = ClientConfigurationProvider.class, value = "local")
   private LocalClientConfigurationProvider m_local;

   @Inject(type = ClientConfigurationProvider.class, value = "remote")
   private RemoteClientConfigurationProvider m_remote;

   private ClientConfiguration m_config;

   private Logger m_logger;

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   @Override
   public ClientConfiguration getConfig() {
      if (m_config == null) {
         m_config = m_remote.getConfigure();

         if (m_config == null) {
            m_config = m_local.getConfigure();
         }

         if (m_config == null) {
            m_logger.warn("Unable to load client configuration, CAT is DISABLED!");
         }

         if (m_config == null) {
            m_config = new DefaultClientConfiguration();
         }
      }

      return m_config;
   }
}

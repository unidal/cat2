package org.unidal.cat.config.internals;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.config.ClientConfiguration;
import org.unidal.cat.config.ClientConfigurationManager;
import org.unidal.cat.config.ClientConfigurationProvider;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ClientConfigurationManager.class)
public class DefaultClientConfigurationManager implements ClientConfigurationManager, LogEnabled {
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

         m_config = new NullClientConfiguration();
      }

      return m_config;
   }

   public class NullClientConfiguration implements ClientConfiguration {
      @Override
      public List<InetSocketAddress> getServerNodes(String type) {
         return Collections.emptyList();
      }

      @Override
      public boolean isEnabled() {
         return false;
      }
   }
}

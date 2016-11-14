package org.unidal.cat.config.internals;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.config.ClientConfiguration;
import org.unidal.cat.config.ClientConfigurationManager;
import org.unidal.cat.config.ClientConfigurationProvider;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ClientConfigurationManager.class)
public class DefaultClientConfigurationManager implements ClientConfigurationManager, Initializable, LogEnabled {
   @Inject("local")
   private ClientConfigurationProvider m_local;

   @Inject("remote")
   private ClientConfigurationProvider m_remote;

   private ClientConfiguration m_config;

   private Logger m_logger;

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   @Override
   public ClientConfiguration getConfig() {
      return m_config;
   }

   @Override
   public void initialize() throws InitializationException {
      m_config = m_remote.getConfigure();

      if (m_config == null) {
         m_config = m_local.getConfigure();
      }

      if (m_config == null) {
         m_logger.warn("Unable to load client configuration, CAT is disabled!");
      }

      m_config = new NullClientConfiguration();
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

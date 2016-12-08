package org.unidal.cat.config.internals;

import org.unidal.cat.config.ClientConfiguration;
import org.unidal.cat.config.ClientConfigurationProvider;
import org.unidal.cat.config.ClientEnvironmentSettings;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Server;

@Named(type = ClientConfigurationProvider.class, value = "local")
public class LocalClientConfigurationProvider implements ClientConfigurationProvider {
   @Inject
   private ClientEnvironmentSettings m_settings;

   @Override
   public ClientConfiguration getConfigure() {
      ClientConfig config = m_settings.getClientXml();

      if (config != null) {
         DefaultClientConfiguration configure = new DefaultClientConfiguration();

         for (Server server : config.getServers()) {
            configure.addServerForTree(server.getIp(), server.getPort());
         }

         configure.setEnabled(config.isEnabled());
         configure.setDomain(m_settings.getDomain());
         return configure;
      }

      return null;
   }
}

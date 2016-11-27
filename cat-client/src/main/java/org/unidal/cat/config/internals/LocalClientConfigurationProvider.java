package org.unidal.cat.config.internals;

import java.io.File;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.config.ClientConfiguration;
import org.unidal.cat.config.ClientConfigurationProvider;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultSaxParser;

@Named(type = ClientConfigurationProvider.class, value = "local")
public class LocalClientConfigurationProvider implements ClientConfigurationProvider, LogEnabled {
   @Inject
   private ClientSettings m_settings;

   private Logger m_logger;

   @Override
   public ClientConfiguration getConfigure() {
      File file = m_settings.getClientXmlFile();

      if (file.canRead()) {
         try {
            String xml = Files.forIO().readFrom(file, "utf-8");
            ClientConfig config = DefaultSaxParser.parse(xml);
            DefaultClientConfiguration configure = new DefaultClientConfiguration();

            for (Server server : config.getServers()) {
               configure.addServerForTree(server.getIp(), server.getPort());
            }

            configure.setEnabled(config.isEnabled());
            return configure;
         } catch (Throwable e) {
            m_logger.warn(String.format("Error when loading configure from file(%s)!", file), e);
         }
      }

      return null;
   }

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }
}

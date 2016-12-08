package org.unidal.cat.config.internals;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.config.ClientConfiguration;
import org.unidal.cat.config.ClientConfigurationProvider;
import org.unidal.cat.config.ClientEnvironmentSettings;
import org.unidal.cat.config.route.entity.RoutePolicy;
import org.unidal.cat.config.route.transform.DefaultSaxParser;
import org.unidal.helper.Files;
import org.unidal.helper.Urls;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ClientConfigurationProvider.class, value = "remote")
public class RemoteClientConfigurationProvider implements ClientConfigurationProvider, LogEnabled {
   @Inject
   private ClientEnvironmentSettings m_settings;

   @Inject
   private ServerDiscovery m_discovery;

   private Logger m_logger;

   private String buildUrl(InetSocketAddress server) {
      String host = server.getHostString();
      int port = server.getPort();
      String domain = m_settings.getDomain();
      String url = String.format(m_settings.getRemoteConfigUrlPattern(), host, port, domain);

      return url;
   }

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   private String fetchConfig(String url) throws IOException {
      int timeout = 1000; // 1s
      Map<String, List<String>> headers = new HashMap<String, List<String>>();
      InputStream in = Urls.forIO().connectTimeout(timeout).readTimeout(timeout) //
            .header("Accept-Encoding", "gzip").openStream(url, headers);

      if ("[gzip]".equals(String.valueOf(headers.get("Content-Encoding")))) {
         in = new GZIPInputStream(in);
      }

      String xml = Files.forIO().readFrom(in, "utf-8");

      return xml;
   }

   @Override
   public ClientConfiguration getConfigure() {
      List<InetSocketAddress> servers = m_discovery.getMetaServers();

      for (InetSocketAddress server : servers) {
         if (m_settings.isServerMode() || server.getHostString().equals("127.0.0.1")) {
            // Build the client configuration directly
            DefaultClientConfiguration configure = new DefaultClientConfiguration();

            configure.addServerForTree(server.getAddress().getHostAddress(), 2280);
            configure.setEnabled(true);
            configure.setDomain(m_settings.getDomain());
            return configure;
         } else {
            try {
               String url = buildUrl(server);
               String xml = fetchConfig(url);
               RoutePolicy policy = DefaultSaxParser.parse(xml);
               DefaultClientConfiguration config = new DefaultClientConfiguration(policy);

               config.setDomain(m_settings.getDomain());
               return config;
            } catch (Throwable e) {
               m_logger.warn(String.format("Error when loading configure from server(%s)!", server), e);
            }
         }
      }

      return null;
   }
}

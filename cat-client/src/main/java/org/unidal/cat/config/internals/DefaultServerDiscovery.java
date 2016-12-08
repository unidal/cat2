package org.unidal.cat.config.internals;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.CatConstant;
import org.unidal.cat.config.ClientEnvironmentSettings;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Server;

@Named(type = ServerDiscovery.class)
public class DefaultServerDiscovery implements ServerDiscovery, LogEnabled {
   @Inject
   private ClientEnvironmentSettings m_settings;

   private Logger m_logger;

   private List<InetSocketAddress> buildAddresses(Map<String, String> map) {
      List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();

      for (Map.Entry<String, String> e : map.entrySet()) {
         String host = e.getKey();
         String port = e.getValue();
         InetSocketAddress address;

         try {
            if (port.length() == 0) {
               address = new InetSocketAddress(host, getDefaultServerHttpPort());
            } else {
               address = new InetSocketAddress(host, Integer.parseInt(port));
            }

            addresses.add(address);
         } catch (Throwable t) {
            m_logger.warn(String.format("Bad CAT server(%s:port) discovered!", host, port), t);
         }
      }

      return addresses;
   }

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   protected int getDefaultServerHttpPort() {
      return m_settings.getDefaultCatServerPort();
   }

   @Override
   public List<InetSocketAddress> getMetaServers() {
      String servers = getServersFromSystemProperties();

      if (servers == null) {
         servers = getServersFromClientXml();
      }

      if (servers == null) {
         servers = getServersByDetection();
      }

      if (servers == null) {
         servers = getServersFromEnvironmentVariable();
      }

      if (servers == null) {
         servers = getServersByCatHost();
      }

      if (servers == null) {
         servers = getServersFromSettings();
      }

      if (servers == null) {
         return Collections.emptyList();
      } else {
         Map<String, String> map = Splitters.by(',', ':').trim().split(servers);

         return buildAddresses(map);
      }
   }

   protected String getServersByCatHost() {
      return null;
   }

   protected String getServersByDetection() {
      // Designed to be overridden
      return null;
   }

   protected String getServersFromClientXml() {
      ClientConfig config = m_settings.getClientXml();

      if (config != null) {
         StringBuilder sb = new StringBuilder(256);

         for (Server server : config.getServers()) {
            if (sb.length() > 0) {
               sb.append(',');
            }

            sb.append(server.getIp()).append(':').append(server.getHttpPort());
         }

         return sb.toString();
      }

      return null;
   }

   protected String getServersFromEnvironmentVariable() {
      return System.getenv(CatConstant.ENV_CAT_SERVERS);
   }

   protected String getServersFromSettings() {
      return m_settings.getDefaultCatServer();
   }

   protected String getServersFromSystemProperties() {
      return System.getProperty(CatConstant.PROPERTY_CAT_SERVERS);
   }
}

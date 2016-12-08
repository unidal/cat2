package org.unidal.cat.transport;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.config.ClientConfigurationManager;
import org.unidal.helper.Files;
import org.unidal.helper.Splitters;
import org.unidal.helper.Urls;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.net.SocketAddressProvider;

import com.dianping.cat.configuration.KVConfig;
import com.site.helper.JsonBuilder;

@Named(type = SocketAddressProvider.class)
public class DefaultSocketAddressProvider implements SocketAddressProvider, LogEnabled {
   @Inject
   private ClientConfigurationManager m_configManager;

   private JsonBuilder m_jsonBuilder = new JsonBuilder();

   private Logger m_logger;

   private long m_lastCheckTime;

   private List<InetSocketAddress> m_addresses;

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   private String fetchRouterConfig() {
      String url = m_configManager.getConfig().getServerConfigUrl();

      try {
         InputStream inputstream = Urls.forIO().readTimeout(200).connectTimeout(100).openStream(url);
         String content = Files.forIO().readFrom(inputstream, "utf-8");
         KVConfig config = (KVConfig) m_jsonBuilder.parse(content.trim(), KVConfig.class);
         String value = config.getValue("routers");

         return value;
      } catch (Exception e) {
         m_logger.warn("Error when fetching router config from " + url + "!", e);
      }

      return null;
   }

   @Override
   public synchronized List<InetSocketAddress> getAddresses() {
      long now = System.currentTimeMillis();

      if (now - m_lastCheckTime > m_configManager.getConfig().getRefreshInterval()) {
         String routerConfig = fetchRouterConfig();
         List<InetSocketAddress> addresses = parse(routerConfig);

         m_lastCheckTime = now;
         m_addresses = addresses;
      }

      return m_addresses;
   }

   private List<InetSocketAddress> parse(String routerConfig) {
      List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();
      Map<String, String> map = Splitters.by(';', ':').trim().split(routerConfig);

      try {
         for (Map.Entry<String, String> e : map.entrySet()) {
            addresses.add(new InetSocketAddress(e.getKey(), Integer.parseInt(e.getValue())));
         }
      } catch (Exception e) {
         m_logger.error("Error when parsing router config: " + routerConfig, e);
      }

      return addresses;
   }
}

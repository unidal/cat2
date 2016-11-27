package org.unidal.cat.config.internals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Inets;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.transform.DefaultSaxParser;

/**
 * This class is key entry point for CAT to access the environment.
 * 
 * @author qmwu2000 <qmwu2000@gmail.com>
 */
@Named(type = ClientSettings.class)
public class DefaultClientSettings implements ClientSettings, Initializable, LogEnabled {
   private static final String CLIENT_XML = "/META-INF/cat/client.xml";

   private static final String APP_PROPERTIES = "/META-INF/app.properties";

   private Properties m_properties = new Properties();

   private ClientConfig m_config;

   private Logger m_logger;

   private String m_home;

   private String m_domain;

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   @Override
   public String getCatHome() {
      if (m_home == null) {
         String home = System.getProperty("cat.home", null);

         if (home == null) {
            home = System.getenv("CAT_HOME");
         }

         if (home != null) {
            m_home = home;
         } else {
            m_home = m_properties.getProperty("cat.home", "/data/appdatas/cat");
         }
      }

      return m_home;
   }

   @Override
   public File getClientXmlFile() {
      File file = new File(getCatHome(), "client.xml");

      try {
         return file.getCanonicalFile();
      } catch (IOException e) {
         return file;
      }
   }

   @Override
   public String getDefaultCatServer() {
      String server = m_properties.getProperty("cat.default.server");

      return server;
   }

   @Override
   public int getDefaultCatServerPort() {
      return 8080;
   }

   @Override
   public String getDomain() {
      if (m_domain == null) {
         // try app.properties
         String domain = m_properties.getProperty("app.name");

         // try client.xml
         if (domain == null && m_config != null) {
            Map<String, Domain> domains = m_config.getDomains();

            for (Domain d : domains.values()) {
               domain = d.getId(); // first domain
            }
         }

         if (domain != null) {
            m_domain = domain;
         } else {
            m_domain = "Unknown";
         }
      }

      return m_domain;
   }

   @Override
   public String getHostName() {
      return Inets.IP4.getLocalHostName();
   }

   @Override
   public String getIpAddress() {
      return Inets.IP4.getLocalHostAddress();
   }

   @Override
   public String getRemoteConfigUrlPattern() {
      return "http://%s:%s/cat/config/client?domain=%s";
   }

   private InputStream getResource(String resource) {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);

      if (in == null) {
         in = DefaultClientSettings.class.getResourceAsStream(resource);
      }

      return in;
   }

   @Override
   public void initialize() throws InitializationException {
      loadAppProperties();
      loadClientConfig();
   }

   private void loadAppProperties() {
      InputStream in = getResource(APP_PROPERTIES);

      if (in != null) {
         Properties properties = new Properties();

         try {
            properties.load(in);
            m_properties = properties;
         } catch (Throwable e) {
            m_logger.warn(String.format("Error when loading %s!", APP_PROPERTIES), e);
         } finally {
            try {
               in.close();
            } catch (Exception e) {
               // ignore it
            }
         }
      }
   }

   private void loadClientConfig() {
      InputStream in = getResource(CLIENT_XML);

      if (in != null) {
         try {
            m_config = DefaultSaxParser.parse(in);
         } catch (Exception e) {
            m_logger.warn(String.format("Error when loading %s!", CLIENT_XML), e);
         } finally {
            try {
               in.close();
            } catch (Exception e) {
               // ignore it
            }
         }
      }
   }
}

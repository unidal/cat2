package org.unidal.cat.config.internals;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.CatConstant;
import org.unidal.cat.config.ClientEnvironmentSettings;
import org.unidal.helper.Files;
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
@Named(type = ClientEnvironmentSettings.class)
public class DefaultClientEnvironmentSettings implements ClientEnvironmentSettings, Initializable, LogEnabled {
   private static final String RESOURCE_CLIENT_XML = "/META-INF/cat/client.xml";

   private static final String RESOURCE_APP_PROPERTIES = "/META-INF/app.properties";

   private static final String KEY_APP_NAME = "app.name";

   private static final String KEY_CAT_DEFAULT_SERVER = "cat.default.server";

   private static final String KEY_CAT_HOME = "cat.home";

   private static final String DOMAIN_UNKNOWN = "Unknown";

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
         String home = System.getProperty(CatConstant.PROPERTY_CAT_HOME, null);

         if (home == null) {
            home = System.getenv(CatConstant.ENV_CAT_HOME);
         }

         if (home != null) {
            m_home = home;
         } else {
            m_home = m_properties.getProperty(KEY_CAT_HOME, CatConstant.DEFAULT_CAT_HOME);
         }
      }

      return m_home;
   }

   @Override
   public ClientConfig getClientXml() {
      File file = new File(getCatHome(), CatConstant.FILE_CLIENT_XML);

      if (file.canRead()) {
         try {
            String xml = Files.forIO().readFrom(file, "utf-8");
            ClientConfig config = DefaultSaxParser.parse(xml);

            return config;
         } catch (Exception e) {
            m_logger.warn(String.format("Error when loading config from %s!", file), e);
         }
      }

      return null;
   }

   @Override
   public String getDefaultCatServer() {
      String server = m_properties.getProperty(KEY_CAT_DEFAULT_SERVER);

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
         String domain = null;

         if (domain == null) {
            domain = System.getProperty(CatConstant.PROPERTY_CAT_DOMAIN);
         }

         if (domain == null && m_properties != null) {
            domain = m_properties.getProperty(KEY_APP_NAME);
         }

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
            m_domain = DOMAIN_UNKNOWN;
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
      return "http://%s:%s/cat/config/service?op=client&domain=%s"; // TODO fix the hard code
   }

   private InputStream getResource(String resource) {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);

      if (in == null) {
         in = DefaultClientEnvironmentSettings.class.getResourceAsStream(resource);
      }

      return in;
   }

   @Override
   public void initialize() throws InitializationException {
      loadAppProperties();
      loadClientConfig();
   }

   private void loadAppProperties() {
      InputStream in = getResource(RESOURCE_APP_PROPERTIES);

      if (in != null) {
         Properties properties = new Properties();

         try {
            properties.load(in);
            m_properties = properties;
         } catch (Throwable e) {
            m_logger.warn(String.format("Error when loading %s!", RESOURCE_APP_PROPERTIES), e);
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
      InputStream in = getResource(RESOURCE_CLIENT_XML);

      if (in != null) {
         try {
            m_config = DefaultSaxParser.parse(in);
         } catch (Exception e) {
            m_logger.warn(String.format("Error when loading %s!", RESOURCE_CLIENT_XML), e);
         } finally {
            try {
               in.close();
            } catch (Exception e) {
               // ignore it
            }
         }
      }
   }

   @Override
   public boolean isTestMode() {
      return "true".equals(System.getProperty(CatConstant.PROPERTY_TEST_MODE));
   }

   @Override
   public boolean isServerMode() {
      return "true".equals(System.getProperty(CatConstant.PROPERTY_SERVER_MODE));
   }
}

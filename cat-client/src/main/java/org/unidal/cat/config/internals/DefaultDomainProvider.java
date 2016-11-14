package org.unidal.cat.config.internals;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.transform.DefaultSaxParser;

@Named(type = DomainProvider.class)
public class DefaultDomainProvider implements DomainProvider, Initializable, LogEnabled {
   private static final String CLIENT_XML = "/META-INF/cat/client.xml";

   private static final String APP_PROPERTIES = "/META-INF/app.properties";

   private Logger m_logger;

   private String m_domain;

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   @Override
   public String getDomain() {
      return m_domain;
   }

   @Override
   public void initialize() throws InitializationException {
      m_domain = loadFromAppProperties();

      if (m_domain == null) {
         m_domain = loadFromClientXml();
      }

      if (m_domain == null) {
         throw new InitializationException(String.format("No domain was found in resource %s or %s!"));
      }
   }

   private String loadFromAppProperties() {
      InputStream in = loadResource(APP_PROPERTIES);

      if (in != null) {
         Properties properties = new Properties();

         try {
            properties.load(in);
         } catch (Exception e) {
            m_logger.warn(String.format("Error when loading app.name from %s!", APP_PROPERTIES), e);
         } finally {
            try {
               in.close();
            } catch (Exception e) {
               // ignore it
            }
         }

         String domain = properties.getProperty("app.name");

         if (domain != null) {
            return domain;
         }
      }

      return null;
   }

   private String loadFromClientXml() {
      InputStream in = loadResource(CLIENT_XML);

      if (in != null) {
         try {
            ClientConfig config = DefaultSaxParser.parse(in);
            Map<String, Domain> domains = config.getDomains();

            for (Domain domain : domains.values()) {
               return domain.getId(); // first domain
            }
         } catch (Exception e) {
            m_logger.warn(String.format("Error when loading domain from %s!", CLIENT_XML), e);
         } finally {
            try {
               in.close();
            } catch (Exception e) {
               // ignore it
            }
         }
      }

      return null;
   }

   private InputStream loadResource(String resource) {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);

      if (in == null) {
         in = DefaultDomainProvider.class.getResourceAsStream(resource);
      }

      return in;
   }
}

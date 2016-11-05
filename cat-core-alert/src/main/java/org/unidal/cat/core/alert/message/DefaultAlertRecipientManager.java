package org.unidal.cat.core.alert.message;

import static org.unidal.cat.core.config.spi.ConfigStoreManager.GROUP_ALERT;

import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.alert.rules.entity.AlertModelDef;
import org.unidal.cat.core.alert.rules.transform.DefaultSaxParser;
import org.unidal.cat.core.config.spi.ConfigChangeListener;
import org.unidal.cat.core.config.spi.ConfigException;
import org.unidal.cat.core.config.spi.ConfigStore;
import org.unidal.cat.core.config.spi.ConfigStoreManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = AlertRecipientManager.class)
public class DefaultAlertRecipientManager implements AlertRecipientManager, ConfigChangeListener, Initializable,
      LogEnabled {
   private static final String NAME = "recipient";

   @Inject
   private ConfigStoreManager m_manager;

   private AlertModelDef m_model;

   private Logger m_logger;

   @Override
   public Map<String, AlertRecipient> getRecipients(AlertMessage message) {
      return null;
   }

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   @Override
   public void initialize() throws InitializationException {
      m_manager.register(GROUP_ALERT, NAME, this);

      ConfigStore store = m_manager.getConfigStore(GROUP_ALERT, NAME);
      String config = store.getConfig();

      if (config != null) {
         try {
            m_model = DefaultSaxParser.parse(config);
         } catch (Exception e) {
            throw new InitializationException(String.format("Error when parsing config model(%s:%s)! %s", GROUP_ALERT,
                  NAME, config), e);
         }
      } else {
         Cat.logEvent("ConfigMissing", GROUP_ALERT + ":" + NAME);
         m_logger.warn("No configure found for " + GROUP_ALERT + ":" + NAME);
      }

      if (m_model == null) {
         m_model = new AlertModelDef();
      }
   }

   @Override
   public void onChanged(String config) throws ConfigException {
      if (config != null) {
         try {
            m_model = DefaultSaxParser.parse(config);
         } catch (Exception e) {
            throw new ConfigException(String.format("Error when parsing config model(%s:%s)! %s", GROUP_ALERT, NAME,
                  config), e);
         }
      }
   }
}

package org.unidal.cat.core.alert.rule;

import static org.unidal.cat.core.alert.config.AlertConfigStoreGroup.ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.alert.rule.entity.AlertModelDef;
import org.unidal.cat.core.alert.rule.entity.AlertRuleSetDef;
import org.unidal.cat.core.alert.rule.transform.DefaultSaxParser;
import org.unidal.cat.core.config.spi.ConfigChangeListener;
import org.unidal.cat.core.config.spi.ConfigException;
import org.unidal.cat.core.config.spi.ConfigStore;
import org.unidal.cat.core.config.spi.ConfigStoreManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = RuleService.class)
public class DefaultRuleService implements RuleService, ConfigChangeListener, Initializable, LogEnabled {
   private static final String NAME = "rule";

   @Inject
   private ConfigStoreManager m_manager;

   private AlertModelDef m_model;

   private Logger m_logger;

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   @Override
   public Set<String> getAttributes(String type, String name) {
      Set<String> set = new TreeSet<String>();

      for (AlertRuleSetDef ruleSet : m_model.getRuleSets()) {
         if (ruleSet.getTypeName().equals(type)) {
            String val = ruleSet.getDynamicAttribute(name);

            if (val != null) {
               set.add(val);
            }
         }
      }

      return set;
   }

   @Override
   public List<AlertRuleSetDef> getRuleSetByAttribute(String type, String name, String value) {
      List<AlertRuleSetDef> list = new ArrayList<AlertRuleSetDef>();

      for (AlertRuleSetDef ruleSet : m_model.getRuleSets()) {
         if (ruleSet.getTypeName().equals(type)) {
            String val = ruleSet.getDynamicAttribute(name);

            if (val != null && val.equals(value)) {
               list.add(ruleSet);
            }
         }
      }

      return list;
   }

   @Override
   public List<AlertRuleSetDef> getRuleSets() {
      return m_model.getRuleSets();
   }

   @Override
   public Set<String> getTypes() {
      Set<String> set = new TreeSet<String>();

      for (AlertRuleSetDef ruleSet : m_model.getRuleSets()) {
         set.add(ruleSet.getTypeName());
      }

      return set;
   }

   @Override
   public void initialize() throws InitializationException {
      m_manager.register(ID, NAME, this);

      ConfigStore store = m_manager.getConfigStore(ID, NAME);
      String config = store.getConfig();

      if (config != null) {
         try {
            m_model = DefaultSaxParser.parse(config);
         } catch (Exception e) {
            throw new InitializationException(String.format("Error when parsing config model(%s:%s)! %s", ID, NAME,
                  config), e);
         }
      } else {
         m_logger.warn("No configure found for " + ID + ":" + NAME);
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
            throw new ConfigException(String.format("Error when parsing config model(%s:%s)! %s", ID, NAME, config), e);
         }
      }
   }
}

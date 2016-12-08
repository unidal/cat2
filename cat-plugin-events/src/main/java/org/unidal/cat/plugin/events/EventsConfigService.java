package org.unidal.cat.plugin.events;

import static org.unidal.cat.core.report.config.ReportConfigStoreGroup.ID;
import static org.unidal.cat.plugin.events.EventsConstants.NAME;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.config.spi.ConfigChangeListener;
import org.unidal.cat.core.config.spi.ConfigException;
import org.unidal.cat.core.config.spi.ConfigStore;
import org.unidal.cat.core.config.spi.ConfigStoreManager;
import org.unidal.cat.plugin.events.config.entity.ConfigModel;
import org.unidal.cat.plugin.events.config.entity.EventsConfigModel;
import org.unidal.cat.plugin.events.config.transform.DefaultSaxParser;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

@Named
public class EventsConfigService implements Initializable, ConfigChangeListener {
   @Inject
   private ConfigStoreManager m_manager;

   private Map<String, Pair<String, Boolean>> m_matchedTypes = new HashMap<String, Pair<String, Boolean>>();

   private Map<String, Pair<String, Boolean>> m_startingTypes = new HashMap<String, Pair<String, Boolean>>();

   private ThreadLocal<Pair<String, Boolean>> m_cachedName = new ThreadLocal<Pair<String, Boolean>>();

   @Override
   public void initialize() throws InitializationException {
      m_manager.register(ID, NAME, this);

      ConfigStore store = m_manager.getConfigStore(ID, NAME);
      String config = store.getConfig();

      try {
         if (config != null) {
            EventsConfigModel root = DefaultSaxParser.parse(config);

            initialize(root);
         }
      } catch (Exception e) {
         throw new InitializationException("Invalid events config:\r\n" + config, e);
      }
   }

   private void initialize(EventsConfigModel root) {
      Map<String, Pair<String, Boolean>> startingTypes = new HashMap<String, Pair<String, Boolean>>();
      Map<String, Pair<String, Boolean>> matchedTypes = new HashMap<String, Pair<String, Boolean>>();

      for (ConfigModel config : root.getConfigs()) {
         String type = config.getType().trim();
         String name = config.getName().trim();
         Pair<String, Boolean> pair = new Pair<String, Boolean>();

         if (name.endsWith("*")) { // suffix with "*"
            pair.setKey(name.substring(0, name.length() - 1));
            pair.setValue(true);
         } else {
            pair.setKey(name);
            pair.setValue(false);
         }

         if (type.endsWith("*")) { // suffix with "*"
            startingTypes.put(type.substring(0, type.length() - 1), pair);
         } else {
            matchedTypes.put(type, pair);
         }
      }

      m_startingTypes = startingTypes;
      m_matchedTypes = matchedTypes;
   }

   public boolean isEligible(String type) {
      Pair<String, Boolean> pair = m_matchedTypes.get(type);

      if (pair != null) {
         m_cachedName.set(pair);
         return true;
      }

      for (Map.Entry<String, Pair<String, Boolean>> e : m_startingTypes.entrySet()) {
         if (type.startsWith(e.getKey())) {
            m_cachedName.set(e.getValue());
            return true;
         }
      }

      m_cachedName.set(null);
      return false;
   }

   public boolean isEligible(String type, String name) {
      Pair<String, Boolean> pair = m_cachedName.get();

      if (pair == null) {
         return false;
      } else if (pair.getValue().booleanValue()) { // suffix with "*"
         return name.startsWith(pair.getKey());
      } else {
         return pair.getKey().equals(name);
      }
   }

   @Override
   public void onChanged(String config) throws ConfigException {
      try {
         if (config != null) {
            EventsConfigModel root = DefaultSaxParser.parse(config);

            initialize(root);
         }
      } catch (Exception e) {
         throw new ConfigException("Invalid events config:\r\n" + config, e);
      }
   }

   public void reset() {
      m_cachedName.remove();
   }
}

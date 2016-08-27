package org.unidal.cat.plugin.transactions;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.plugin.transactions.config.entity.ConfigModel;
import org.unidal.cat.plugin.transactions.config.entity.TransactionsConfigModel;
import org.unidal.cat.plugin.transactions.config.transform.DefaultSaxParser;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

@Named
public class TransactionsConfigService implements Initializable {
   private Map<String, Pair<String, Boolean>> m_matchedTypes = new HashMap<String, Pair<String, Boolean>>();

   private Map<String, Pair<String, Boolean>> m_startingTypes = new HashMap<String, Pair<String, Boolean>>();

   private ThreadLocal<Pair<String, Boolean>> m_cachedName = new ThreadLocal<Pair<String, Boolean>>();

   @Override
   public void initialize() throws InitializationException {
      try {
         InputStream in = getClass().getResourceAsStream("config/transactions-config.xml"); // TODO for test
         TransactionsConfigModel root = DefaultSaxParser.parse(in);

         initialize(root);
      } catch (Exception e) {
         throw new InitializationException("Unable to load transactions-config.xml!", e);
      }
   }

   private void initialize(TransactionsConfigModel root) {
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
            m_startingTypes.put(type.substring(0, type.length() - 1), pair);
         } else {
            m_matchedTypes.put(type, pair);
         }
      }
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

   public void reset() {
      m_cachedName.remove();
   }
}

package org.unidal.cat.core.alert.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.alert.data.entity.AlertDataStore;
import org.unidal.cat.core.alert.rule.entity.AlertRuleDef;
import org.unidal.cat.core.alert.rule.entity.AlertRuleSetDef;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.LookupException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named
public class RuleEvaluatorManager extends ContainerHolder implements Initializable {
   @Inject
   private RuleService m_service;

   private Map<String, Entry> m_map = new HashMap<String, Entry>();

   private Entry getEntry(String type, boolean createIfNotExist) {
      Entry entry = m_map.get(type);

      if (createIfNotExist && entry == null) {
         entry = new Entry(type);
         m_map.put(type, entry);
      }

      return entry;
   }

   public List<RuleEvaluator> getEvaluators(String type) {
      Entry entry = getEntry(type, false);

      if (entry == null) {
         return Collections.emptyList();
      } else {
         return entry.getEvaluators();
      }
   }

   public AlertDataStore getStore(String type) {
      Entry entry = getEntry(type, false);

      if (entry == null) {
         return null;
      } else {
         return entry.getStore();
      }
   }

   @Override
   public void initialize() throws InitializationException {
      List<AlertRuleSetDef> ruleSets = m_service.getRuleSets();

      for (AlertRuleSetDef ruleSet : ruleSets) {
         String type = ruleSet.getTypeName();
         Entry entry = getEntry(type, true);

         for (AlertRuleDef rule : ruleSet.getRules()) {
            try {
               RuleEvaluator evaluator = lookup(RuleEvaluator.class, type);

               rule.setRuleSet(ruleSet);
               evaluator.initialize(entry.getStore(), rule);

               entry.add(evaluator);
            } catch (LookupException e) {
               throw new InitializationException(String.format("Unable to initialize rule handler(%s)!", type), e);
            }
         }
      }
   }

   static class Entry {
      private String m_type;

      private AlertDataStore m_store;

      private List<RuleEvaluator> m_evaluators;

      public Entry(String type) {
         m_type = type;
         m_store = new AlertDataStore(type);
         m_evaluators = new ArrayList<RuleEvaluator>();
      }

      public void add(RuleEvaluator evaluator) {
         m_evaluators.add(evaluator);
      }

      public List<RuleEvaluator> getEvaluators() {
         return m_evaluators;
      }

      public AlertDataStore getStore() {
         return m_store;
      }

      public String getType() {
         return m_type;
      }

      @Override
      public String toString() {
         return String.format("%s[type=%s, evaluators=%s]", getClass().getSimpleName(), m_type, m_evaluators.size());
      }
   }
}
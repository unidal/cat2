package org.unidal.cat.core.alert.metric.handler;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.alert.metric.Metrics;
import org.unidal.cat.core.alert.rule.AlertRuleService;
import org.unidal.cat.core.alert.rules.entity.AlertRule;
import org.unidal.cat.core.alert.rules.entity.AlertRuleSegment;
import org.unidal.cat.core.alert.rules.entity.AlertRuleSet;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named
public class HandlerManager extends ContainerHolder implements Initializable {
   @Inject
   private AlertRuleService m_service;

   private List<Handler> m_handlers;

   public List<Handler> getHandlers(Metrics metrics) {
      return m_handlers;
   }

   @Override
   public void initialize() throws InitializationException {
      List<AlertRuleSet> ruleSets = m_service.getRuleSets();
      List<Handler> handlers = new ArrayList<Handler>();

      for (AlertRuleSet ruleSet : ruleSets) {
         for (AlertRuleSegment segment : ruleSet.getRuleSegments()) {
            segment.setRuleSet(ruleSet);

            for (AlertRule rule : segment.getRules()) {
               Handler handler = lookup(Handler.class, ruleSet.getTypeName());

               rule.setRuleSegment(segment);
               handler.initialize(rule);
               handlers.add(handler);
            }
         }
      }

      m_handlers = handlers;
   }
}
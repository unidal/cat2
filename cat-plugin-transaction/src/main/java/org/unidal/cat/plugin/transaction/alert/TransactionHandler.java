package org.unidal.cat.plugin.transaction.alert;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.alert.message.DefaultAlertMessage;
import org.unidal.cat.core.alert.metric.handler.AlertMessageSink;
import org.unidal.cat.core.alert.metric.handler.ConditionEvaluator;
import org.unidal.cat.core.alert.metric.handler.Handler;
import org.unidal.cat.core.alert.metric.handler.TimeMatcher;
import org.unidal.cat.core.alert.rules.entity.AlertCondition;
import org.unidal.cat.core.alert.rules.entity.AlertRule;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = Handler.class, value = TransactionConstants.NAME)
public class TransactionHandler implements Handler<TransactionMetrics> {
   @Inject
   private AlertMessageSink m_sink;

   private AlertRule m_rule;

   private TransactionTimeWindow m_window;

   private TimeMatcher m_matcher;

   private List<ConditionEvaluator> m_evaluators = new ArrayList<ConditionEvaluator>();

   private void fireAlert() {
      DefaultAlertMessage alert = new DefaultAlertMessage(m_rule, m_window);

      m_sink.add(alert);
   }

   public void handle(TransactionMetrics metrics) {
      if (metrics.getAlertMetric() == null) {
         m_window.shift();
      } else if (m_matcher.matches(System.currentTimeMillis())) {
         boolean passed = true;

         m_window.addMetrics(metrics);

         for (ConditionEvaluator evaluator : m_evaluators) {
            if (!evaluator.evaluate(m_window)) {
               passed = false;
               break;
            }
         }

         if (passed) {
            fireAlert();
            m_window.reset();
         }
      } else {
         // ignore it
      }
   }

   @Override
   public void initialize(AlertRule rule) {
      String fieldName = rule.getRuleSegment().getRuleSet().getFieldName();

      m_rule = rule;
      m_window = new TransactionTimeWindow(rule.getTimeWindow(), fieldName);
      m_matcher = new TimeMatcher(rule.getRuleSegment());

      for (AlertCondition condition : m_rule.getConditions()) {
         m_evaluators.add(new ConditionEvaluator(condition));
      }
   }
}
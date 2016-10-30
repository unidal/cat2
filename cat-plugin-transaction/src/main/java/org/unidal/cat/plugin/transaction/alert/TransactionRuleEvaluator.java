package org.unidal.cat.plugin.transaction.alert;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.alert.data.entity.AlertDataSegment;
import org.unidal.cat.core.alert.data.entity.AlertDataShard;
import org.unidal.cat.core.alert.data.entity.AlertDataStore;
import org.unidal.cat.core.alert.message.DefaultAlertMessage;
import org.unidal.cat.core.alert.metric.Metrics;
import org.unidal.cat.core.alert.metric.handler.AlertMessageSink;
import org.unidal.cat.core.alert.metric.handler.RuleEvaluator;
import org.unidal.cat.core.alert.metric.handler.TimeMatcher;
import org.unidal.cat.core.alert.rules.entity.AlertConditionDef;
import org.unidal.cat.core.alert.rules.entity.AlertRuleDef;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = RuleEvaluator.class, value = TransactionConstants.NAME, instantiationStrategy = Named.PER_LOOKUP)
public class TransactionRuleEvaluator implements RuleEvaluator {
   @Inject
   private AlertMessageSink m_sink;

   private AlertRuleDef m_rule;

   private TimeMatcher m_matcher;

   private List<ConditionEvaluator> m_evaluators = new ArrayList<ConditionEvaluator>();

   private boolean[] m_passes;

   private AlertDataSegment m_segment;

   @Override
   public void evaluate() {
      if (m_matcher.matches(System.currentTimeMillis())) {
         boolean passed = true;

         for (ConditionEvaluator evaluator : m_evaluators) {
            if (!evaluator.evaluate()) {
               passed = false;
               break;
            }
         }

         tryFireAlert(passed);
      }
   }

   @Override
   public void initialize(AlertDataStore store, AlertRuleDef rule) {
      m_segment = store.findOrCreateSegment(rule.getRuleSet().getId());
      m_rule = rule;
      m_matcher = new TimeMatcher(rule);
      m_passes = new boolean[rule.getTimeWindow()];
      m_segment.setStore(store);

      for (AlertConditionDef condition : rule.getConditions()) {
         m_evaluators.add(new ConditionEvaluator(m_segment, condition));
      }
   }

   private void tryFireAlert(boolean passed) {
      int len = m_passes.length;

      for (int i = len - 1; i > 0; i--) {
         m_passes[i] = m_passes[i - 1];
      }

      m_passes[0] = passed;

      if (passed) {
         boolean shouldFire = true;

         for (boolean pass : m_passes) {
            if (!pass) {
               shouldFire = false;
               break;
            }
         }

         if (shouldFire) {
            DefaultAlertMessage alert = new DefaultAlertMessage(m_rule, m_segment);

            m_passes = new boolean[len]; // reset
            m_sink.add(alert);
         }
      }
   }

   static class ConditionEvaluator {
      private ConditionFunction m_function;

      private ConditionOpertor m_operator;

      private double m_operand;

      public ConditionEvaluator(AlertDataSegment segment, AlertConditionDef condition) {
         m_function = new ConditionFunction(condition.getFunction(), segment, condition.getField());
         m_operator = new ConditionOpertor(condition.getOp());
         m_operand = Double.parseDouble(condition.getValue());
      }

      public boolean evaluate() {
         double value = m_function.evaluate();

         return m_operator.evaluate(value, m_operand);
      }

      @Override
      public String toString() {
         return String.format("%s %s %s", m_function, m_operator, m_operand);
      }
   }

   static class ConditionFunction {
      private String m_function;

      private AlertDataSegment m_segment;

      private String m_field;

      public ConditionFunction(String function, AlertDataSegment segment, String field) {
         m_function = function;
         m_segment = segment;
         m_field = field;
      }

      public double evaluate() {
         double sum = 0;
         int count = 0;

         for (AlertDataShard shard : m_segment.getShards()) {
            List<Metrics> list = shard.getMetricsList();
            Metrics metrics = list.get(list.size() - 1);
            String value = metrics.getAlertMetric().get(m_field);

            sum += value == null ? 0 : Double.parseDouble(value);
            count++;
         }

         if ("avg".equals(m_function)) {
            return sum / count;
         } else if ("sum".equals(m_function)) {
            return sum;
         } else {
            return 0;
         }
      }

      @Override
      public String toString() {
         return String.format("%s(%s)", m_function, m_field);
      }
   }

   static class ConditionOpertor {
      private String m_op;

      public ConditionOpertor(String op) {
         m_op = op;
      }

      public boolean evaluate(double v1, double v2) {
         if ("ge".equals(m_op)) {
            return v1 >= v2;
         } else if ("le".equals(m_op)) {
            return v1 <= v2;
         } else if ("eq".equals(m_op)) {
            return v1 == v2;
         } else {
            return false;
         }
      }

      @Override
      public String toString() {
         return m_op;
      }
   }
}
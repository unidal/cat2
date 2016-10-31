package org.unidal.cat.core.alert.rule;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.alert.data.entity.AlertDataSegment;
import org.unidal.cat.core.alert.data.entity.AlertDataShard;
import org.unidal.cat.core.alert.data.entity.AlertDataStore;
import org.unidal.cat.core.alert.message.AlertMessageSink;
import org.unidal.cat.core.alert.message.DefaultAlertMessage;
import org.unidal.cat.core.alert.metric.Metrics;
import org.unidal.cat.core.alert.model.entity.AlertMetric;
import org.unidal.cat.core.alert.rules.entity.AlertConditionDef;
import org.unidal.cat.core.alert.rules.entity.AlertRuleDef;
import org.unidal.lookup.annotation.Inject;

public abstract class AbstractRuleEvaluator implements RuleEvaluator {
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

   protected abstract double handleFunction(String function, String field, List<AlertMetric> metrics);

   protected abstract boolean handleOperator(String op, double v1, double v2);

   @Override
   public void initialize(AlertDataStore store, AlertRuleDef rule) {
      m_segment = store.findOrCreateSegment(rule.getRuleSet().getId());
      m_rule = rule;
      m_matcher = new TimeMatcher(rule);
      m_passes = new boolean[rule.getTimeWindow()];
      m_segment.setStore(store);

      for (AlertConditionDef condition : rule.getConditions()) {
         ConditionFunction function = new ConditionFunction(condition.getFunction(), m_segment, condition.getField());

         m_evaluators.add(new ConditionEvaluator(function, condition));
      }
   }

   protected void tryFireAlert(boolean passed) {
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
            m_passes = new boolean[len]; // reset
            m_sink.add(new DefaultAlertMessage(m_rule, m_segment));
         }
      }
   }

   class ConditionEvaluator {
      private AlertConditionDef m_condition;

      private ConditionFunction m_function;

      private double m_operand;

      public ConditionEvaluator(ConditionFunction function, AlertConditionDef condition) {
         m_condition = condition;
         m_function = function;
         m_operand = Double.parseDouble(condition.getValue());
      }

      public boolean evaluate() {
         double value = m_function.execute();

         return handleOperator(m_condition.getOp(), value, m_operand);
      }

      @Override
      public String toString() {
         return String.format("%s %s %s", m_function, m_condition.getOp(), m_operand);
      }
   }

   protected class ConditionFunction {
      private String m_function;

      private AlertDataSegment m_segment;

      private String m_field;

      public ConditionFunction(String function, AlertDataSegment segment, String field) {
         m_function = function;
         m_segment = segment;
         m_field = field;
      }

      public double execute() {
         List<AlertMetric> metrics = new ArrayList<AlertMetric>();

         for (AlertDataShard shard : m_segment.getShards()) {
            List<Metrics> list = shard.getMetricsList();

            metrics.add(list.get(list.size() - 1).getAlertMetric());
         }

         return handleFunction(m_function, m_field, metrics);
      }

      @Override
      public String toString() {
         return String.format("%s(%s)", m_function, m_field);
      }
   }
}
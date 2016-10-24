package org.unidal.cat.core.alert.metric.handler;

import org.unidal.cat.core.alert.rules.entity.AlertCondition;

public class ConditionEvaluator {
   private Operator m_operator;

   private Functor m_functor;

   private double m_operand;

   public ConditionEvaluator(AlertCondition condition) {
      m_functor = Functor.getByName(condition.getType());
      m_operator = Operator.getByName(condition.getOp());
      m_operand = Double.parseDouble(condition.getValue());
   }

   public boolean evaluate(TimeWindow window) {
      double value = m_functor.evaluate(window);

      return m_operator.evaluate(value, m_operand);
   }
}
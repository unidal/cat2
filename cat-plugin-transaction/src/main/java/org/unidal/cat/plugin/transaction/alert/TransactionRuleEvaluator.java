package org.unidal.cat.plugin.transaction.alert;

import java.util.List;

import org.unidal.cat.core.alert.metric.handler.AbstractRuleEvaluator;
import org.unidal.cat.core.alert.metric.handler.RuleEvaluator;
import org.unidal.cat.core.alert.model.entity.AlertMetric;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.lookup.annotation.Named;

@Named(type = RuleEvaluator.class, value = TransactionConstants.NAME, instantiationStrategy = Named.PER_LOOKUP)
public class TransactionRuleEvaluator extends AbstractRuleEvaluator {
   @Override
   protected double handleFunction(String function, String field, List<AlertMetric> metrics) {
      double sum = 0;
      int count = 0;

      for (AlertMetric metric : metrics) {
         String value = metric.get(field);

         sum += value == null ? 0 : Double.parseDouble(value);
         count++;
      }

      if ("avg".equals(function)) {
         return sum / count;
      } else if ("sum".equals(function)) {
         return sum;
      } else {
         return 0;
      }
   }

   @Override
   protected boolean handleOperator(String op, double v1, double v2) {
      if ("ge".equals(op)) {
         return v1 >= v2;
      } else if ("le".equals(op)) {
         return v1 <= v2;
      } else if ("eq".equals(op)) {
         return v1 == v2;
      } else {
         return false;
      }
   }
}
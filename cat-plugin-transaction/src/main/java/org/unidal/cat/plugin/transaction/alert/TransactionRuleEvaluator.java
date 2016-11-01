package org.unidal.cat.plugin.transaction.alert;

import java.util.List;

import org.unidal.cat.core.alert.rule.AbstractRuleEvaluator;
import org.unidal.cat.core.alert.rule.RuleEvaluator;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.lookup.annotation.Named;

@Named(type = RuleEvaluator.class, value = TransactionConstants.NAME, instantiationStrategy = Named.PER_LOOKUP)
public class TransactionRuleEvaluator extends AbstractRuleEvaluator<TransactionMetrics> {
   @Override
   protected double handleFunction(String function, String field, List<TransactionMetrics> lasts) {
      TransactionField f = TransactionField.getByName(field);
      double sum = 0;
      int count = 0;

      for (TransactionMetrics metric : lasts) {
         double value = f.getValue(metric);

         sum += value;
         count++;
      }

      if ("avg".equals(function)) {
         return count == 0 ? 0 : sum / count;
      } else if ("sum".equals(function)) {
         return sum;
      } else {
         return 0;
      }
   }
}
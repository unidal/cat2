package org.unidal.cat.core.alert.rule;

import org.unidal.cat.core.alert.data.entity.AlertDataStore;
import org.unidal.cat.core.alert.rule.entity.AlertRuleDef;

public interface RuleEvaluator {
   public void evaluate();

   public void initialize(AlertDataStore store, AlertRuleDef rule);
}

package org.unidal.cat.core.alert.metric.handler;

import org.unidal.cat.core.alert.data.entity.AlertDataStore;
import org.unidal.cat.core.alert.rules.entity.AlertRuleDef;

public interface RuleEvaluator {
   public void evaluate();

   public void initialize(AlertDataStore store, AlertRuleDef rule);
}

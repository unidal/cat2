package org.unidal.cat.plugin.transaction.alert;

import org.unidal.cat.core.alert.rule.AlertRuleField;

public enum TransactionField implements AlertRuleField {
   HITS,

   FAILURES,

   DURATION,

   FAILURE_RATE;
}
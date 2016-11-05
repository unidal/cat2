package org.unidal.cat.core.alert.message;

import org.unidal.cat.core.alert.data.entity.AlertDataSegment;
import org.unidal.cat.core.alert.rule.entity.AlertRuleDef;

public class DefaultAlertMessage implements AlertMessage {
   private AlertRuleDef m_rule;

   private AlertDataSegment m_segment;

   public DefaultAlertMessage(AlertRuleDef rule, AlertDataSegment segment) {
      m_rule = rule;
      m_segment = segment;
   }

   @Override
   public AlertRuleDef getRule() {
      return m_rule;
   }

   @Override
   public AlertDataSegment getSegment() {
      return m_segment;
   }

   @Override
   public String toString() {
      return "rule: " + m_rule.getRuleSet().getId() + "\r\n" + m_segment;
   }
}

package org.unidal.cat.core.alert.message;

import org.unidal.cat.core.alert.message.AlertMessage;
import org.unidal.cat.core.alert.metric.handler.TimeWindow;
import org.unidal.cat.core.alert.rules.entity.AlertRule;

public class DefaultAlertMessage implements AlertMessage {
   private AlertRule m_rule;

   private TimeWindow m_window;

   public DefaultAlertMessage(AlertRule rule, TimeWindow window) {
      m_rule = rule;
      m_window = window;
   }

   @Override
   public String toString() {
      return m_rule.getRuleSegment().getRuleSet().toString() + m_window;
   }
}

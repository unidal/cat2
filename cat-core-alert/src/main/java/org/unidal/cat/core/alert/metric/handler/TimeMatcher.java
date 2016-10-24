package org.unidal.cat.core.alert.metric.handler;

import java.util.concurrent.TimeUnit;

import org.unidal.cat.core.alert.rules.entity.AlertRuleSegment;

public class TimeMatcher {
   private long m_startMinutes;

   private long m_endMinutes;

   public TimeMatcher(AlertRuleSegment segment) {
      String startTime = segment.getStartTime();
      String endTime = segment.getEndTime();

      m_startMinutes = toMinutes(startTime);
      m_endMinutes = toMinutes(endTime);
   }

   public boolean matches(long timestamp) {
      long minutes = TimeUnit.MILLISECONDS.toMinutes(timestamp) % 1440;

      return m_startMinutes <= minutes && minutes < m_endMinutes;
   }

   private long toMinutes(String str) {
      int len = str.length();
      long minutes = 0;
      int part = 0;

      for (int i = 0; i < len; i++) {
         char ch = str.charAt(i);

         if (Character.isDigit(ch)) {
            part = part * 10 + (ch - '0');
         } else if (ch == ':') {
            minutes += part * 60;
            part = 0;
         } else {
            throw new IllegalStateException("Invalid time format: " + str + "!");
         }
      }

      minutes += part;

      return minutes;
   }
}
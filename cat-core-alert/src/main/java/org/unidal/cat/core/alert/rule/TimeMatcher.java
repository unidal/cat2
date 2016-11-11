package org.unidal.cat.core.alert.rule;

import java.util.Calendar;

import org.unidal.cat.core.alert.rule.entity.AlertRuleDef;

public class TimeMatcher {
   private long m_startMinutes;

   private long m_endMinutes;

   public TimeMatcher(AlertRuleDef rule) {
      String startTime = rule.getStartTime();
      String endTime = rule.getEndTime();

      m_startMinutes = toMinutes(startTime, 0);
      m_endMinutes = toMinutes(endTime, 1440);
   }

   public boolean matches(long timestamp) {
      Calendar cal = Calendar.getInstance();

      cal.setTimeInMillis(timestamp);

      int hour = cal.get(Calendar.HOUR_OF_DAY);
      int minute = cal.get(Calendar.MINUTE);
      long minutes = hour * 60 + minute;

      return m_startMinutes <= minutes && minutes < m_endMinutes;
   }

   private long toMinutes(String str, int defaultValue) {
      try {
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
      } catch (Exception e) {
         // ignore it
      }

      return defaultValue;
   }
}
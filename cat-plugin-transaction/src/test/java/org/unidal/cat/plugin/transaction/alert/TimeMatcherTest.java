package org.unidal.cat.plugin.transaction.alert;

import java.util.Calendar;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.core.alert.rule.TimeMatcher;
import org.unidal.cat.core.alert.rule.entity.AlertRuleDef;

public class TimeMatcherTest {
   @Test
   public void test() {
      checkInRange("00:00", "24:00", 0, 120, 1439);

      checkInRange("01:00", "24:00", 60, 120, 1439);
      checkNotInRange("01:00", "24:00", 0, 59);

      checkInRange("08:00", "24:00", 480, 1439);
      checkNotInRange("08:00", "24:00", 0, 59, 120, 479);

      checkInRange("08:00", "18:00", 480, 1079);
      checkNotInRange("08:00", "18:00", 0, 59, 120, 479, 1080, 1439);
   }

   private void checkInRange(String startTime, String endTime, int... minutes) {
      AlertRuleDef segment = new AlertRuleDef().setStartTime(startTime).setEndTime(endTime);
      TimeMatcher matcher = new TimeMatcher(segment);
      Calendar cal = Calendar.getInstance();

      for (int minute : minutes) {
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, minute);

         long timestamp = cal.getTime().getTime();

         Assert.assertEquals(true, matcher.matches(timestamp));
      }
   }

   private void checkNotInRange(String startTime, String endTime, int... minutes) {
      AlertRuleDef segment = new AlertRuleDef().setStartTime(startTime).setEndTime(endTime);
      TimeMatcher matcher = new TimeMatcher(segment);
      Calendar cal = Calendar.getInstance();

      for (int minute : minutes) {
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, minute);

         long timestamp = cal.getTime().getTime();

         Assert.assertEquals(false, matcher.matches(timestamp));
      }
   }
}

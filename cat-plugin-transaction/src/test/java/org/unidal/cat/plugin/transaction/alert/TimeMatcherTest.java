package org.unidal.cat.plugin.transaction.alert;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.core.alert.metric.handler.TimeMatcher;
import org.unidal.cat.core.alert.rules.entity.AlertRuleDef;

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

      for (int minute : minutes) {
         Assert.assertEquals(true, matcher.matches(minute * 60 * 1000L));
      }
   }

   private void checkNotInRange(String startTime, String endTime, int... minutes) {
      AlertRuleDef segment = new AlertRuleDef().setStartTime(startTime).setEndTime(endTime);
      TimeMatcher matcher = new TimeMatcher(segment);

      for (int minute : minutes) {
         Assert.assertEquals(false, matcher.matches(minute * 60 * 1000L));
      }
   }
}

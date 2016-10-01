package org.unidal.cat.core.alert.esper;

import org.junit.Test;
import org.unidal.cat.core.alert.model.entity.AlertMetric;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class AlertModelTest extends EpserTestCase {
   @Test
   public void test() {
      registerEventType("Transaction", AlertMetric.class);
      register(
            "select type,sum(cast(dynamicAttributes('count'),int)) as sum,avg(cast(dynamicAttributes('count'),int)) as avg from Transaction(type='Transaction').win:length_batch(3)",
            new MetricListener());

      registerEventType("Event", AlertMetric.class);
      register("select type,sum(cast(dynamicAttributes('count'),int)) as sum,avg(cast(dynamicAttributes('count'),int)) as avg from Event(type='Event').win:length_batch(4)",
            new MetricListener());

      sendEvent(new AlertMetric().setType("Transaction").set("count", "1"));
      sendEvent(new AlertMetric().setType("Transaction").set("count", "3"));
      sendEvent(new AlertMetric().setType("Transaction").set("count", "5"));

      sendEvent(new AlertMetric().setType("Event").set("count", "2"));
      sendEvent(new AlertMetric().setType("Event").set("count", "4"));
      sendEvent(new AlertMetric().setType("Event").set("count", "6"));
      sendEvent(new AlertMetric().setType("Event").set("count", "8"));
   }

   class MetricListener implements UpdateListener {
      public void update(EventBean[] newEvents, EventBean[] oldEvents) {
         if (newEvents != null) {
            String type = (String) newEvents[0].get("type");
            Integer sum = (Integer) newEvents[0].get("sum");
            Double avg = (Double) newEvents[0].get("avg");

            System.out.println(String.format("Total count of %s is %s, average is %s", type, sum, avg));
         }
      }
   }
}

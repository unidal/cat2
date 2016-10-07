package org.unidal.cat.core.alert.esper;

import org.junit.Test;
import org.unidal.cat.core.alert.model.entity.AlertMetric;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class AlertModelTest extends EpserTestCase {
   @Test
   public void test() {
      registerEventType("Transaction", AlertMetric.class);
      register("select type,ip,cast(dynamicAttributes('count'),int) as count," //
            + "sum(cast(dynamicAttributes('count'),int)) as sum," //
            + "avg(cast(dynamicAttributes('count'),int)) as avg " //
            + "from Transaction(type='Transaction').win:length_batch(3)", new MetricListener());

      registerEventType("Event", AlertMetric.class);
      register("select type,cast(dynamicAttributes('count2'),int) as count," //
            + "sum(cast(dynamicAttributes('count2'),int)) as sum," //
            + "avg(cast(dynamicAttributes('count2'),int)) as avg " //
            + "from Event(type='Event').win:length_batch(4)", new MetricListener());

      sendEvent(new AlertMetric().setType("Transaction").set("count", "1").setIp("ip1"));
      sendEvent(new AlertMetric().setType("Transaction").set("count", "3").setIp("ip2"));
      sendEvent(new AlertMetric().setType("Transaction").set("count", "5").setIp("ip3"));

      sendEvent(new AlertMetric().setType("Event").set("count2", "2"));
      sendEvent(new AlertMetric().setType("Event").set("count2", "4"));
      sendEvent(new AlertMetric().setType("Event").set("count2", "6"));
      sendEvent(new AlertMetric().setType("Event").set("count2", "8"));
   }

   class MetricListener implements UpdateListener {
      public void update(EventBean[] newEvents, EventBean[] oldEvents) {
         if (newEvents != null) {
            System.out.println("size: " + newEvents.length);

            for (EventBean event : newEvents) {
               String type = (String) event.get("type");
               Integer sum = (Integer) event.get("sum");
               Double avg = (Double) event.get("avg");

               System.out.println(event.getUnderlying());
               System.out.println(String.format("Total count of %s is %s, average is %s", type, sum, avg));

               if (event.getEventType().isProperty("ip")) {
                  String ip = (String) event.get("ip");
                  System.out.println("ip: " + ip);
               }
            }
         }
      }
   }
}

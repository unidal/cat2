package org.unidal.cat.core.alert.esper;

import junit.framework.Assert;

import org.junit.Test;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class AppleTest extends EpserTestCase {
   @Test
   public void test() {
      registerEventType("Apple", Apple.class);
      register("select avg(price) from Apple.win:length_batch(3)", new AppleListener());

      sendEvent(new Apple(1, 5));
      sendEvent(new Apple(2, 2));
      sendEvent(new Apple(3, 5));
   }

   class Apple {
      private int m_id;

      private int m_price;

      public Apple(int id, int price) {
         m_id = id;
         m_price = price;
      }

      public int getId() {
         return m_id;
      }

      public int getPrice() {
         return m_price;
      }
   }

   class AppleListener implements UpdateListener {
      public void update(EventBean[] newEvents, EventBean[] oldEvents) {
         if (newEvents != null) {
            Double avg = (Double) newEvents[0].get("avg(price)");

            Assert.assertEquals(4.0, avg);
            System.out.println("Apple's average price is " + avg);
         }
      }
   }
}

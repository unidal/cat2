package com.dianping.cat.message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.Cat;

@RunWith(JUnit4.class)
public class TransactionTest {
   private final int ITER = 100;

   @Test
   public void testNormal() throws InterruptedException {
      System.setProperty("devMode", "true");

      for (int i = 0; i < ITER; i++) {
         Transaction t = Cat.getProducer().newTransaction("URL", "MyPage");

         try {
            Cat.logEvent("URL", "MyPage");
            // do your business here
            t.addData("k1", "v1");
            t.addData("k2", "v2");
            t.addData("k3", "v3");
            Thread.sleep(3);

            t.setStatus(Message.SUCCESS);
         } catch (Exception e) {
            t.setStatus(e);
         } finally {
            t.complete();
         }
      }

      Thread.sleep(1000);
   }
}

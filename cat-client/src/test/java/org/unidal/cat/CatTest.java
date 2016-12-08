package org.unidal.cat;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.Cat.Context;
import org.unidal.lookup.ComponentTestCase;

public class CatTest extends ComponentTestCase {
   @Test
   public void clientAndService() {
      Context ctx = new Context() {
         @Override
         public void addProperty(String key, String value) {
         }

         @Override
         public String getProperty(String key) {
            return null;
         }
      };

      Cat.logRemoteCallClient(ctx, "domain");
      Cat.logRemoteCallServer(ctx);
   }

   @Test
   public void disable() {
      Cat.disable();

      Assert.assertEquals(false, Cat.isEnabled());
   }

   @Test
   public void messageId() {
      String id = Cat.getMessageId();

      System.out.println(id);
   }

   @Test
   public void metric() {
      Cat.count("metric1");
      Cat.count("metric2", 3);
      Cat.duration("metric3", 3000L);
   }

   @Test
   public void setClientXml() {
      Cat.setProperty("cat.client.xml", new File("client.xml"));

      Cat.logError(new Exception());
   }

   @Test
   public void message() {
      Cat.logEvent("Type", "Name");
      Cat.logError(new Exception());
      Cat.logError("message", new Exception());
   }
}

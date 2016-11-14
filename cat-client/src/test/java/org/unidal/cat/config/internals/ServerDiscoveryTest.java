package org.unidal.cat.config.internals;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

public class ServerDiscoveryTest extends ComponentTestCase {
   private static Map<String, String> MAP = new HashMap<String, String>();

   @Test
   public void testFromEnvironmentVariable() throws Exception {
      defineComponent(ServerDiscovery.class, MockServerDiscovery.class);

      MAP.clear();
      MAP.put("env.CAT", "127.0.1.1,127.0.1.2");

      try {
         ServerDiscovery d = lookup(ServerDiscovery.class);
         String servers = d.getMetaServers().toString();

         Assert.assertEquals("[/127.0.1.1:2281, /127.0.1.2:2281]", servers);
      } finally {
      }
   }

   @Test
   public void testFromSystemProperties() {
      System.setProperty("cat", "127.0.0.1,127.0.0.2:2281,127.0.0.3:2282");

      try {
         ServerDiscovery d = lookup(ServerDiscovery.class);
         String servers = d.getMetaServers().toString();

         Assert.assertEquals("[/127.0.0.1:2281, /127.0.0.2:2281, /127.0.0.3:2282]", servers);
      } finally {
         System.getProperties().remove("cat");
      }
   }

   public static class MockServerDiscovery extends DefaultServerDiscovery {
      @Override
      protected int getDefaultServerHttpPort() {
         return 2281;
      }

      @Override
      protected String getServersFromClientXml() {
         return null;
      }

      @Override
      protected String getServersFromEnvironmentVariable() {
         return MAP.get("env.CAT");
      }
   }
}

package org.unidal.cat.config.internals;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.config.ClientEnvironmentSettings;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Server;

public class ServerDiscoveryTest extends ComponentTestCase {
   @Test
   public void getByCatHost() throws Exception {
      defineComponent(ServerDiscovery.class, MockServerDiscovery.class);

      // for test only
      System.setProperty("host", "true");

      try {
         ServerDiscovery d = lookup(ServerDiscovery.class);
         String servers = d.getMetaServers().toString();

         Assert.assertEquals("[/127.0.0.1:2281]", servers);
      } finally {
         System.getProperties().remove("host");
      }
   }

   @Test
   public void getByDetection() throws Exception {
      defineComponent(ServerDiscovery.class, MockServerDiscovery.class);

      // for test only
      System.setProperty("detection", "true");

      try {
         ServerDiscovery d = lookup(ServerDiscovery.class);
         String servers = d.getMetaServers().toString();

         Assert.assertEquals("[/127.0.0.1:2281, /127.0.0.2:2281, /127.0.0.3:2282, /127.0.0.4:2282]", servers);
      } finally {
         System.getProperties().remove("detection");
      }
   }

   @Test
   public void getFromClientXml() throws Exception {
      defineComponent(ClientEnvironmentSettings.class, MockSettings.class);
      defineComponent(ServerDiscovery.class, MockServerDiscovery.class).req(ClientEnvironmentSettings.class);

      // for test only
      System.setProperty("client.xml", "true");

      try {
         ServerDiscovery d = lookup(ServerDiscovery.class);
         String servers = d.getMetaServers().toString();

         Assert.assertEquals("[/127.0.4.1:2281, /127.0.4.2:2282]", servers);
      } finally {
         System.getProperties().remove("client.xml");
      }
   }

   @Test
   public void getFromEnvironmentVariable() throws Exception {
      defineComponent(ServerDiscovery.class, MockServerDiscovery.class);

      // for test only
      System.setProperty("env", "127.0.2.1,127.0.2.2");

      try {
         ServerDiscovery d = lookup(ServerDiscovery.class);
         String servers = d.getMetaServers().toString();

         Assert.assertEquals("[/127.0.2.1:2281, /127.0.2.2:2281]", servers);
      } finally {
         System.getProperties().remove("env");
      }
   }

   @Test
   public void getFromSystemProperties() {
      System.setProperty("cat.servers", "127.0.3.1:2281,127.0.3.2:2281,127.0.3.3:2282");

      try {
         ServerDiscovery d = lookup(ServerDiscovery.class);
         String servers = d.getMetaServers().toString();

         Assert.assertEquals("[/127.0.3.1:2281, /127.0.3.2:2281, /127.0.3.3:2282]", servers);
      } finally {
         System.getProperties().remove("cat.servers");
      }
   }

   @Named(type = ServerDiscovery.class)
   public static class MockServerDiscovery extends DefaultServerDiscovery {
      @Override
      protected int getDefaultServerHttpPort() {
         return 2281;
      }

      @Override
      protected String getServersByCatHost() {
         String host = System.getProperty("host");

         if ("true".equals(host)) {
            return "127.0.0.1";
         } else {
            return null;
         }
      }

      @Override
      protected String getServersByDetection() {
         String detection = System.getProperty("detection");

         if ("true".equals(detection)) {
            return "127.0.0.1,127.0.0.2:2281,127.0.0.3:2282,127.0.0.4:2282";
         } else {
            return null;
         }
      }

      @Override
      protected String getServersFromClientXml() {
         String client = System.getProperty("client.xml");

         if ("true".equals(client)) {
            return super.getServersFromClientXml();
         } else {
            return null;
         }
      }

      @Override
      protected String getServersFromEnvironmentVariable() {
         // real environment variable is CAT
         return System.getProperty("env");
      }
   }

   @Named(type = ClientEnvironmentSettings.class)
   public static class MockSettings extends DefaultClientEnvironmentSettings {
      @Override
      public ClientConfig getClientXml() {
         ClientConfig config = new ClientConfig();

         config.addServer(new Server("127.0.4.1").setHttpPort(2281));
         config.addServer(new Server("127.0.4.2").setHttpPort(2282));

         return config;
      }
   }
}

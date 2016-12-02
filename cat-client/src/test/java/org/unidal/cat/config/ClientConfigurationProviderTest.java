package org.unidal.cat.config;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.unidal.cat.config.internals.DefaultServerDiscovery;
import org.unidal.cat.config.internals.DefaultClientEnvironmentSettings;
import org.unidal.cat.config.internals.ServerDiscovery;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Named;

public class ClientConfigurationProviderTest extends ComponentTestCase {
   @Test
   public void testRemote() throws Exception {
      defineComponent(ServerDiscovery.class, MockServerDiscovery.class);
      defineComponent(ClientEnvironmentSettings.class, MockSettings.class);

      ClientConfigurationProvider provider = lookup(ClientConfigurationProvider.class, "remote");

      System.out.println(provider.getConfigure());
   }

   @Named(type = ClientEnvironmentSettings.class)
   public static class MockSettings extends DefaultClientEnvironmentSettings {
      @Override
      public String getRemoteConfigUrlPattern() {
         return "file:///%s/%s/%s"; // TODO
      }

      @Override
      public String getDomain() {
         return "mock";
      }
   }

   @Named(type = ServerDiscovery.class)
   public static class MockServerDiscovery extends DefaultServerDiscovery {
      @Override
      public List<InetSocketAddress> getMetaServers() {
         return Arrays.asList(new InetSocketAddress("localhost", 2281));
      }
   }
}

package org.unidal.cat.core.config.spi;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.cat.core.config.spi.internals.DefaultConfigStore;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.extension.RoleHintEnabled;

public class ConfigStoreManagerTest extends ComponentTestCase {
   private void check(String group, String name, String expected) {
      ConfigStoreManager manager = lookup(ConfigStoreManager.class);
      ConfigStore store = manager.getConfigStore(group, name);

      Assert.assertEquals(expected, store.getConfig());
   }

   @Test
   public void test() throws Exception {
      defineComponent(ConfigStore.class, "mock:mock", MockConfigStore.class);
      defineComponent(ConfigStoreGroup.class, "mock", MockConfigStoreGroup.class);
      defineComponent(ConfigStoreGroup.class, "mock2", MockConfigStoreGroup.class);

      check("mock", "mock", "mock-config");
      check("mock", "mock2", "mock:mock2");
      check("mock2", "mock", "mock2:mock");

      check("unknown", "unknown", null);
   }

   public static class MockConfigStore implements ConfigStore {
      @Override
      public String getConfig() {
         return "mock-config";
      }

      @Override
      public void setConfig(String config) {
      }
   }

   public static class MockConfigStoreGroup implements ConfigStoreGroup, RoleHintEnabled {
      private String m_group;

      @Override
      public ConfigStore getConfigStore(String name) {
         return new DefaultConfigStore(m_group + ":" + name);
      }

      @Override
      public void enableRoleHint(String roleHint) {
         m_group = roleHint;
      }
   }
}

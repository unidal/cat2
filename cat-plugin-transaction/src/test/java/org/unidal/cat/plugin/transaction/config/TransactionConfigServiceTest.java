package org.unidal.cat.plugin.transaction.config;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.core.config.spi.ConfigStore;
import org.unidal.cat.plugin.transaction.TransactionConfigService;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

public class TransactionConfigServiceTest extends ComponentTestCase {
   @Test
   public void test() throws Exception {
      defineComponent(ConfigStore.class, "report:transaction", MockConfigStore.class);

      TransactionConfigService service = lookup(TransactionConfigService.class);

      Assert.assertEquals(true, service.isEligible("URL"));
      Assert.assertEquals(false, service.isEligible("phoenix-agent"));
      Assert.assertEquals(true, service.isEligible("Phoenix"));
   }

   public static class MockConfigStore implements ConfigStore {
      @Override
      public String getConfig() {
         InputStream in = getClass().getResourceAsStream("transaction-config.xml");

         if (in == null) {
            throw new IllegalStateException("Can't find resource: transaction-config.xml!");
         }

         try {
            return Files.forIO().readFrom(in, "utf-8");
         } catch (IOException e) {
            e.printStackTrace();
            return null;
         }
      }

      @Override
      public void setConfig(String config) {
      }
   }
}

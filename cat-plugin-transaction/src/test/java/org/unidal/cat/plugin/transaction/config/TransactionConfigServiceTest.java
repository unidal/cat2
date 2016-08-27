package org.unidal.cat.plugin.transaction.config;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.plugin.transaction.TransactionConfigService;
import org.unidal.lookup.ComponentTestCase;

public class TransactionConfigServiceTest extends ComponentTestCase {
   @Test
   public void test() {
      TransactionConfigService service = lookup(TransactionConfigService.class);

      Assert.assertEquals(true, service.isEligible("URL"));
      Assert.assertEquals(false, service.isEligible("phoenix-agent"));
      Assert.assertEquals(true, service.isEligible("Phoenix"));
   }
}

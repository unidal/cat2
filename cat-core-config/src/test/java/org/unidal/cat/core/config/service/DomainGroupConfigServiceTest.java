package org.unidal.cat.core.config.service;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

public class DomainGroupConfigServiceTest extends ComponentTestCase {
   @Test
   public void test() throws Exception {
      DomainGroupConfigService service = lookup(DomainGroupConfigService.class);

      Assert.assertEquals(true, service.isInGroup("cat", "local", "127.0.0.1"));
      Assert.assertEquals(true, service.isInGroup("cat", "test", "127.0.0.1"));
      
      Assert.assertEquals(true, service.isInGroup("cat", "local", "10.12.34.13"));
      Assert.assertEquals(false, service.isInGroup("cat", "test", "10.12.34.13"));
   }
}

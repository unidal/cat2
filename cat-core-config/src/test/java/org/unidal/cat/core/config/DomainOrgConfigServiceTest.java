package org.unidal.cat.core.config;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

public class DomainOrgConfigServiceTest extends ComponentTestCase {
   @Test
   public void test() {
      DomainOrgConfigService service = lookup(DomainOrgConfigService.class);

      Assert.assertEquals("framework", service.findDepartment("cat"));
      Assert.assertEquals("Unknown", service.findDepartment(null));
      Assert.assertEquals("Unknown", service.findDepartment(""));

      Assert.assertEquals(true, service.isIn("framework", "cat"));
      Assert.assertEquals(false, service.isIn(null, "cat"));
      Assert.assertEquals(false, service.isIn("framework", null));
      Assert.assertEquals(false, service.isIn("framework", "test"));
   }
}

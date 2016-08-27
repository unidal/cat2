package org.unidal.cat.plugin.event.config;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.plugin.event.EventConfigService;
import org.unidal.lookup.ComponentTestCase;

public class EventConfigServiceTest extends ComponentTestCase {
   @Test
   public void test() {
      EventConfigService service = lookup(EventConfigService.class);

      Assert.assertEquals(true, service.isEligible("URL"));
      Assert.assertEquals(false, service.isEligible("phoenix-agent"));
      Assert.assertEquals(true, service.isEligible("Phoenix"));
   }
}

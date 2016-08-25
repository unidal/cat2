package org.unidal.cat.plugin.events.config;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

public class EventsConfigServiceTest extends ComponentTestCase {
   @Test
   public void test() {
      EventsConfigService service = lookup(EventsConfigService.class);

      Assert.assertEquals(true, service.isEligible("URL"));
      Assert.assertEquals(true, service.isEligible("URL", "/any/page"));

      Assert.assertEquals(false, service.isEligible("URL.Forward"));
      Assert.assertEquals(false, service.isEligible("URL.Forward", "/any/page"));

      Assert.assertEquals(true, service.isEligible("SQL"));
      Assert.assertEquals(true, service.isEligible("SQL", "/any/statement"));

      Assert.assertEquals(true, service.isEligible("Cache.Redis"));
      Assert.assertEquals(true, service.isEligible("Cache.Redis", "/any/cache"));

      Assert.assertEquals(true, service.isEligible("Cache.Memcached"));
      Assert.assertEquals(true, service.isEligible("Cache.Memcached", "/any/cache"));

      Assert.assertEquals(false, service.isEligible("Cache"));
      Assert.assertEquals(false, service.isEligible("Cache", "/any/thing"));

      Assert.assertEquals(true, service.isEligible("Call"));
      Assert.assertEquals(true, service.isEligible("Call", "MyService.First"));
      Assert.assertEquals(true, service.isEligible("Call", "MyService.Second"));
      Assert.assertEquals(false, service.isEligible("Call", "OtherService.Any"));
   }
}

package org.unidal.cat.plugin.event.config;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.plugin.event.config.entity.EventConfigModel;
import org.unidal.cat.plugin.event.config.transform.DefaultSaxParser;

public class EventConfigTest {
   @Test
   public void test() throws Exception {
      InputStream in = getClass().getResourceAsStream("event-config.xml");
      EventConfigModel config = DefaultSaxParser.parse(in);

      Assert.assertEquals(2, config.getIgnores().size());
   }
}

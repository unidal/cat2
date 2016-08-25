package org.unidal.cat.plugin.events.config;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.plugin.events.config.entity.EventsConfigModel;
import org.unidal.cat.plugin.events.config.transform.DefaultSaxParser;

public class EventsConfigTest {
   @Test
   public void test() throws Exception {
      InputStream in = getClass().getResourceAsStream("events-config.xml");
      EventsConfigModel config = DefaultSaxParser.parse(in);

      Assert.assertEquals(4, config.getConfigs().size());
   }
}

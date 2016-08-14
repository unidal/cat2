package org.unidal.cat.core.config;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.core.config.domain.group.entity.DomainGroupConfigModel;
import org.unidal.cat.core.config.domain.group.transform.DefaultSaxParser;

public class DomainGroupConfigTest {
   @Test
   public void test() throws Exception {
      InputStream in = getClass().getResourceAsStream("domain-group-config.xml");
      DomainGroupConfigModel config = DefaultSaxParser.parse(in);

      Assert.assertEquals(2, config.getDomains().size());
   }
}

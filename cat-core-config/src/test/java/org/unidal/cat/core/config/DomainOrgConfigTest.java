package org.unidal.cat.core.config;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.core.config.domain.org.entity.DomainOrgConfigModel;
import org.unidal.cat.core.config.domain.org.transform.DefaultSaxParser;

public class DomainOrgConfigTest {
   @Test
   public void test() throws Exception {
      InputStream in = getClass().getResourceAsStream("domain-org-config.xml");
      DomainOrgConfigModel config = DefaultSaxParser.parse(in);

      Assert.assertEquals(3, config.getDepartments().size());
   }
}

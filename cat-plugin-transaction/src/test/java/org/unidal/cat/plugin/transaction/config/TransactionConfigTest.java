package org.unidal.cat.plugin.transaction.config;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.plugin.transaction.config.entity.TransactionConfigModel;
import org.unidal.cat.plugin.transaction.config.transform.DefaultSaxParser;

public class TransactionConfigTest {
   @Test
   public void test() throws Exception {
      InputStream in = getClass().getResourceAsStream("transaction-config.xml");
      TransactionConfigModel config = DefaultSaxParser.parse(in);

      Assert.assertEquals(2, config.getIgnores().size());
   }
}

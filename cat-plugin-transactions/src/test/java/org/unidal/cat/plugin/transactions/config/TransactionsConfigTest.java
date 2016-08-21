package org.unidal.cat.plugin.transactions.config;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.plugin.transactions.config.entity.TransactionsConfigModel;
import org.unidal.cat.plugin.transactions.config.transform.DefaultSaxParser;

public class TransactionsConfigTest {
   @Test
   public void test() throws Exception {
      InputStream in = getClass().getResourceAsStream("transactions-config.xml");
      TransactionsConfigModel config = DefaultSaxParser.parse(in);

      Assert.assertEquals(4, config.getConfigs().size());
   }
}

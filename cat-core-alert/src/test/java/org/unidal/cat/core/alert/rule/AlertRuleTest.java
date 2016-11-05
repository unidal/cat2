package org.unidal.cat.core.alert.rule;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.core.alert.rule.entity.AlertModelDef;
import org.unidal.cat.core.alert.rule.transform.DefaultSaxParser;
import org.unidal.helper.Files;
import org.xml.sax.SAXException;

public class AlertRuleTest {
   @Test
   public void test() throws SAXException, IOException {
      InputStream in = getClass().getResourceAsStream("alert-rule.xml");
      String xml = Files.forIO().readFrom(in, "utf-8");
      AlertModelDef model = DefaultSaxParser.parse(xml);

      Assert.assertEquals(xml.replaceAll("\r\n", "\n"), model.toString().replaceAll("\r\n", "\n"));
   }
}

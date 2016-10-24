package org.unidal.cat.core.alert.rule;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.core.alert.rules.entity.AlertModel;
import org.unidal.cat.core.alert.rules.transform.DefaultSaxParser;
import org.unidal.helper.Files;
import org.xml.sax.SAXException;

public class AlertRulesTest {
   @Test
   public void test() throws SAXException, IOException {
      InputStream in = getClass().getResourceAsStream("alert-rules.xml");
      String xml = Files.forIO().readFrom(in, "utf-8");
      AlertModel model = DefaultSaxParser.parse(xml);

      Assert.assertEquals(xml.replaceAll("\r\n", "\n"), model.toString().replaceAll("\r\n", "\n"));
   }
}

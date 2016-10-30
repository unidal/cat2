package org.unidal.cat.core.alert.data;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.core.alert.data.entity.AlertDataModel;
import org.unidal.cat.core.alert.data.transform.DefaultSaxParser;
import org.unidal.helper.Files;
import org.xml.sax.SAXException;

public class AlertDataTest {
   @Test
   public void test() throws SAXException, IOException {
      InputStream in = getClass().getResourceAsStream("alert-data.xml");
      String xml = Files.forIO().readFrom(in, "utf-8");
      AlertDataModel model = DefaultSaxParser.parse(xml);

      Assert.assertEquals(xml.replaceAll("\r\n", "\n"), model.toString().replaceAll("\r\n", "\n"));
   }
}

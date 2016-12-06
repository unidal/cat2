package com.dianping.cat.configuration;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.transform.DefaultSaxParser;
import com.dianping.cat.configuration.client.transform.DefaultXmlBuilder;

public class ConfigTest {

	@Test
	public void testConfig() throws Exception {
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("config.xml"), "utf-8");
		ClientConfig root = DefaultSaxParser.parse(source);
		String xml = new DefaultXmlBuilder().buildXml(root);
		String expected = source;

		Assert.assertEquals("XML is not well parsed!", expected.replace("\r", ""), xml.replace("\r", ""));
	}

}

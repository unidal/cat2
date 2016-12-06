package com.dianping.cat.configuration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class EnvrionmentTest {

	@Test
	public void test() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(getClass().getResourceAsStream("sg_agent.xml"));

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		Node node = (Node) xpath.evaluate("/SGAgentConf/MnsPath", doc, XPathConstants.NODE);

		final String text = node.getTextContent();
		System.out.println(node.getNodeName() + " " + text);

		System.out.println(text.substring(text.lastIndexOf('/') + 1));
	}
}

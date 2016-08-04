package org.unidal.cat.core.config;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.core.config.domain.entity.DomainConfigModel;
import org.unidal.cat.core.config.domain.transform.DefaultSaxParser;

public class DomainConfigTest {
	@Test
	public void test() throws Exception {
		InputStream in = getClass().getResourceAsStream("domain-config.xml");
		DomainConfigModel config = DefaultSaxParser.parse(in);

		Assert.assertEquals(2, config.getDomains().size());
		;
	}
}

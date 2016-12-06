package com.dianping.cat;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.EnvironmentHelper;
import com.dianping.cat.message.Transaction;

public class CatLazyInitTest extends ComponentTestCase {

	@Before
	public void setUp() throws Exception {
		super.setUp();
		System.setProperty("devMode", "true");
	}

	@Test
	public void test() throws InterruptedException {
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.newTransaction("type", "name");

			t.complete();
		}
		ClientConfigManager configConfig = lookup(ClientConfigManager.class);
		Assert.assertEquals(EnvironmentHelper.loadAppNameByProperty("cat"), configConfig.getDomain());
		Thread.sleep(1000);
	}

	@Test
	public void testInitByDomainIp() throws InterruptedException {
		Cat.initializeByDomain("cat-test", "10.66.13.114");

		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.newTransaction("type", "name");

			t.complete();
		}
		ClientConfigManager configConfig = lookup(ClientConfigManager.class);
		Assert.assertEquals(EnvironmentHelper.loadAppNameByProperty("cat-test"), configConfig.getDomain());
		Thread.sleep(1000);
	}

	@Test
	public void testInitByDomain() throws InterruptedException {
		Cat.initializeByDomain("cat-test");

		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.newTransaction("type", "name");

			t.complete();
		}

		ClientConfigManager configConfig = lookup(ClientConfigManager.class);
		Assert.assertEquals(EnvironmentHelper.loadAppNameByProperty("cat-test"), configConfig.getDomain());
		Thread.sleep(1000);
	}

	@Test
	public void testInitByIps() throws InterruptedException {
		Cat.initializeByDomain("cat-test", 2280, 8080, "10.66.13.114", "10.66.13.115");

		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.newTransaction("type", "name");

			t.complete();
		}

		ClientConfigManager configConfig = lookup(ClientConfigManager.class);
		Assert.assertEquals(EnvironmentHelper.loadAppNameByProperty("cat-test"), configConfig.getDomain());
		Thread.sleep(1000);
	}

}

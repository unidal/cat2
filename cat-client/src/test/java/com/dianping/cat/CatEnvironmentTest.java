package com.dianping.cat;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;

public class CatEnvironmentTest {
	@Test
	public void testWithoutInitialize() throws InterruptedException {
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("TestType", "TestName");

		t.addData("data here");
		t.setStatus("TestStatus");
		t.complete();

		Thread.sleep(100);
		Assert.assertEquals(true, Cat.isInitialized());
	}

	@Test
	public void testWithInitialize() throws InterruptedException {
		Cat.initialize();
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("TestType", "TestName");

		t.addData("data here");
		t.setStatus("TestStatus");
		t.complete();

		Thread.sleep(100);

		Assert.assertEquals(true, Cat.isInitialized());
	}

	@Test
	public void testWithNoExistGlobalConfigInitialize() throws InterruptedException {
		Cat.initialize();
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("TestType", "TestName");

		t.addData("data here");
		t.setStatus("TestStatus");
		t.complete();

		Thread.sleep(100);

		Assert.assertEquals(true, Cat.isInitialized());
	}

	@Test
	public void testJobTest() throws Exception {
		Cat.initialize("192.168.7.70", "192.168.7.71");
		Transaction t = Cat.newTransaction("TestType", "TestName");

		t.addData("data here");
		t.setStatus("TestStatus");
		t.complete();

		Thread.sleep(1000);
	}

	@Test
	public void testEable() throws InterruptedException {
		Cat.initialize("10.66.1.196");
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.newTransaction("TestType", "TestName");

			t.addData("data here");
			t.setStatus("TestStatus");
			t.complete();
		}

		Cat.disable();

		for (int i = 0; i < 100; i++) {
			Transaction t2 = Cat.newTransaction("TestTypeDisable", "TestTypeDisable");

			t2.addData("data here");
			t2.setStatus("TestStatus");
			t2.complete();
		}

		Thread.sleep(1000);
	}

	@Test
	public void testDisableInStart() throws InterruptedException {
		Cat.disable();

		for (int i = 0; i < 100; i++) {
			Transaction t2 = Cat.newTransaction("TestTypeDisable", "TestTypeDisable");

			t2.addData("data here");
			t2.setStatus("TestStatus");
			t2.complete();
		}

		Thread.sleep(1000);
	}

	@Test
	public void testDisableWithEnviroment() throws InterruptedException {
		System.setProperty("CAT_ENABLED", "false");

		for (int i = 0; i < 100; i++) {
			Transaction t2 = Cat.newTransaction("TestTypeDisable", "TestTypeDisable");

			t2.addData("data here");
			t2.setStatus("TestStatus");
			t2.complete();
		}

		Thread.sleep(1000);
	}
}

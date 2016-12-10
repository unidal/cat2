package com.dianping.cat;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;

public class CatTest {

	@Test
	public void test() {
		Cat.newTransaction("logTransaction", "logTransaction");
		Cat.newEvent("logEvent", "logEvent");
		Cat.newTrace("logTrace", "logTrace");
		Throwable cause = new Throwable();
		Cat.logError(cause);
		Cat.logError("message", cause);
		Cat.logTrace("logTrace", "<trace>");
		Cat.logTrace("logTrace", "<trace>", Trace.SUCCESS, "data");
		Cat.logMetricForCount("logMetricForCount");
		Cat.logMetricForCount("logMetricForCount", 4);
		Cat.logMetricForDuration("logMetricForDuration", 100);
		Cat.logEvent("RemoteLink", "Call", Message.SUCCESS, "Cat-0a010680-384736-2061");
		Cat.logEvent("EventType", "EventName");

		Assert.assertEquals(true, Cat.isInitialized());
	}

	@Test
	public void testTransaction() throws InterruptedException {
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.newTransaction("newTransaction", "newTransaction");

			t.setStatus(Transaction.SUCCESS);
			
			t.setDurationInMillis(1000);
			t.complete();
		}
		
		
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.newTransactionWithDuration("test3", "test3", 100);

			t.setStatus(Transaction.SUCCESS);
			t.complete();
		}
		for (int i = 0; i < 100; i++) {
			Cat.newCompletedTransactionWithDuration("test3", "test3", 100);
		}
		Thread.sleep(1000);
	}

}

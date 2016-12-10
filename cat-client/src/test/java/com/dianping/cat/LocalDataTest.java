package com.dianping.cat;

import org.junit.Test;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.DefaultClientConfigManager;
import com.dianping.cat.message.Transaction;

public class LocalDataTest extends ComponentTestCase {

	@Test
	public void test() throws InterruptedException {
		Cat.initializeByDomain("cat");
		ClientConfigManager manager = lookup(ClientConfigManager.class);

		if (manager instanceof DefaultClientConfigManager) {
			((DefaultClientConfigManager) manager).setSample(0.1);
		}
		for (int i = 0; i < 20; i++) {
			Threads.forGroup("cat").start(new SendTask(i));
		}

		Thread.sleep(1000000);
	}

	public static class SendTask implements Task {

		private int m_index;

		public SendTask(int index) {
			m_index = index;
		}

		@Override
		public void run() {
			buildTransactions();
			buildEvents();
			buildMessage();
		}

		private void buildTransactions() {
			Transaction t = Cat.newTransaction("firstTransactionType", "firstTransactionName");

			for (int k = 0; k < 8000; k++) {
				Cat.newCompletedTransactionWithDuration("testTransaction", "testTransaction", 1);
			}
			t.setSuccessStatus();
			t.complete();
		}

		private void buildEvents() {
			Transaction t = Cat.newTransaction("firstTransactionType", "firstTransactionName");

			for (int k = 0; k < 8000; k++) {
				Transaction t2 = Cat.newTransaction("secondTransactionType", "secondTransactionName");
				Cat.logEvent("eventType_" + m_index, "eventName");

				t2.setSuccessStatus();
				t2.complete();
			}
			t.setSuccessStatus();
			t.complete();
		}

		private void buildMessage() {
			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 20; j++) {
					Transaction t = Cat.newTransaction("buildMessage", "topleveltTransactionName" + i);
					for (int k = 0; k < 50; k++) {
						Transaction t2 = Cat.newTransaction("secondBuildMessage", "secondLevelTransactionName" + j);

						Cat.logEvent("eventType" + j, "eventName" + k);
						t2.setSuccessStatus();
						t2.complete();
					}
					t.setSuccessStatus();
					t.complete();
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public String getName() {
			return "send";
		}

		@Override
		public void shutdown() {
		}
	}

}

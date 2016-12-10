package com.dianping.cat.message.internal;

import org.junit.Test;
import org.unidal.helper.Threads;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

public class TruncateMessageTest {

	@Test
	public void test() {
		try {
	      Transaction t = Cat.newTransaction("test", "test");

	      Transaction t1 = Cat.newTransaction("test", "test");

	      for (int i = 0; i < 8000; i++) {
	      	if (i % 4990 == 0) {
	      		Threads.forGroup("cat").start(new AsycThread(t1));
	      	}
	      	Cat.logEvent("event", "event");
	      }
	      t1.complete();
	      t.complete();
	      
	      System.err.println("this is test code");
      } catch (Exception e) {
      	System.err.println("3123123");
      }

		System.err.println("ffff");
	}

	public static class AsycThread implements Runnable {

		private Transaction m_t;

		public AsycThread(Transaction t) {
			m_t = t;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			m_t.complete();
			//m_t.addChild(new DefaultEvent("ttt", "ttt"));
		}

	}

}

package com.dianping.cat.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

public class Log4j2Test {

	private static Logger logger = LogManager.getLogger("HelloLog4j");

	@Test
	public void testLog4j2() throws InterruptedException {
		for (int i = 0; i < 100; i++) {

			Transaction t = Cat.newTransaction("test1", "test2");
			Cat.getManager().setTraceMode(true);

			try {
				int result = 1 / 0;
				logger.info("result=" + result);
			} catch (Exception e) {
				logger.error("occur an error!", e);
				t.setStatus(e);
			}

			t.complete();
		}

		Thread.sleep(1000);

	}
}

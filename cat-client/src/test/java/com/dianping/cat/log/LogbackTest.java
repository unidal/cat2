package com.dianping.cat.log;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

public class LogbackTest {

	public static final Logger logger = LoggerFactory.getLogger(LogbackTest.class);

	@Test
	public void testLogback() throws InterruptedException {
		for (int i = 0; i < 100; i++) {

			Transaction t = Cat.newTransaction("test1", "test3");
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

package com.dianping.cat.message.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.helper.Scanners;
import org.unidal.helper.Scanners.FileMatcher;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;

public class MessageIdFactoryTest {

	@Before
	public void before() {
		final List<String> paths = new ArrayList<String>();
		String base = "/data/appdatas/cat/";
		Scanners.forDir().scan(new File(base), new FileMatcher() {
			@Override
			public Direction matches(File base, String path) {
				if (new File(base, path).isFile()) {
					if (path.indexOf("mark") > -1) {
						paths.add(path);
					}
				}
				return Direction.DOWN;
			}
		});

		for (String path : paths) {
			boolean result = new File(base, path).delete();
			System.err.println("delete " + path + " " + result);
		}
	}

	private void createIndex(MessageIdFactory factory) throws IOException {
		for (int i = 0; i < 10000; i++) {
			factory.getNextId();
		}

		for (int domain = 0; domain < 10; domain++) {
			for (int i = 0; i < 50000; i++) {
				factory.getNextId(domain + "domain");
			}
		}

		for (int domain = 0; domain < 10; domain++) {
			System.out.println(factory.getNextId(domain + "domain"));
		}
	}

	@Test
	public void testCreateAndClose() throws IOException {
		for (int i = 0; i < 1; i++) {
			MessageIdFactory factory = new MessageIdFactory();

			factory.initialize("cat1");
			createIndex(factory);
			factory.saveMark();
			factory.close();

			factory.initialize("cat1");
			factory.close();
		}
	}

	@Test
	public void testCreateRpcContextId() throws IOException {
		MessageIdFactory factory = new MessageIdFactory();

		final String catDomain = "cat1";
		factory.initialize(catDomain);

		for (int domain = 0; domain < 10; domain++) {
			for (int i = 0; i < 50000; i++) {
				factory.getNextId(domain + "domain");
				factory.getNextId(catDomain);
			}
		}

		for (int domain = 0; domain < 10; domain++) {
			final String nextId = factory.getNextId(domain + "domain");
			Assert.assertEquals(MessageId.parse(nextId).getIndex(), 50000);
		}

		final String nextId = factory.getNextId(catDomain);
		Assert.assertEquals(MessageId.parse(nextId).getIndex(), 50000 * 10);
	}

	@Test
	public void testErrorMarkFile() throws IOException {
		InputStream in = getClass().getResourceAsStream("cat-error-mark.mark");
		OutputStream out = new FileOutputStream(new File("/data/appdatas/cat/cat-error-mark.mark"));
		Files.forIO().copy(in, out);
		MessageIdFactory factory = new MessageIdFactory();

		factory.initialize("error-mark");
	}

	@Test
	public void testInit() throws IOException {
		MessageIdFactory factory = new MessageIdFactory();

		for (int i = 0; i < 1; i++) {
			factory.initialize("test");
			createIndex(factory);

			factory.saveMark();
		}
	}

	public void testMutilThread() throws Exception {
		MessageIdFactory factory = new MessageIdFactory();

		factory.initialize("testMutilThread");

		for (int i = 0; i < 20; i++) {
			Threads.forGroup("cat").start(new MultiThread(factory));
		}
		for (int i = 0; i < 10000; i++) {
			createIndex(factory);
		}

		Thread.sleep(100 * 1000);
	}

	public class MultiThread implements Task {

		private MessageIdFactory m_factory;

		public MultiThread(MessageIdFactory factory) {
			m_factory = factory;
		}

		@Override
		public String getName() {
			return "test-name";
		}

		@Override
		public void run() {
			for (int i = 0; i < 1000; i++) {
				try {
					m_factory.saveMark();

					System.out.println("save end");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

}

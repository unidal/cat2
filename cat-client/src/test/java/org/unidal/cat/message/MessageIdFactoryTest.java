package org.unidal.cat.message;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.helper.Joiners;
import org.unidal.helper.Threads;
import org.unidal.lookup.ComponentTestCase;

public class MessageIdFactoryTest extends ComponentTestCase {
	/**
	 * Run it multiple times in console to simulate multiple processes scenario,
	 * 
	 * to ensure multiple processes of same application working well in same one box.
	 */
	public static void main(String... args) throws Exception {
		String type = args.length > 0 ? args[0] : "master";
		String arg0 = args.length > 1 ? args[1] : null;
		MockApplication app = new MockApplication();

		if (type.equals("master")) {
			if (arg0 == null) {
				System.err.println("Options: master <processes>");
				System.exit(1);
			}

			int processes = Integer.parseInt(arg0);

			app.handleMaster(processes);
		} else if (type.equals("slave")) {
			app.handleSlave();
		} else {
			System.err.println("Options: [master|slave] <args>");
			System.exit(1);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testSetup() {
		MessageIdFactory factory = lookup(MessageIdFactory.class);

		factory.getNextId();
	}

	@Test
	public void testParallel() throws Exception {
		File baseDir = new File("target/mark");

		new File(baseDir, "parallel.mark").delete();

		final MessageIdFactory builder = new MockMessageIdBuilder(baseDir, "parallel");
		final Set<String> ids = Collections.synchronizedSet(new HashSet<String>());
		int threads = 10;
		final int messagesPerThread = 1234;
		ExecutorService pool = Threads.forPool().getFixedThreadPool("cat", threads);

		for (int thread = 0; thread < threads; thread++) {
			pool.submit(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < messagesPerThread; i++) {
						ids.add(builder.getNextId());
					}
				}
			});
		}

		pool.shutdown();
		pool.awaitTermination(100, TimeUnit.MILLISECONDS);

		Assert.assertEquals("Not all threads completed in time.", threads * messagesPerThread, ids.size());
		Assert.assertEquals(true,
		      ids.contains(String.format("parallel-c0a81f9e-403215-%s", threads * messagesPerThread - 1)));
		Assert.assertEquals(String.format("parallel-c0a81f9e-403215-%s", threads * messagesPerThread),
		      builder.getNextId());
	}

	@Test
	public void testSerial() throws IOException {
		File baseDir = new File("target/mark");

		new File(baseDir, "serial.mark").delete();

		MessageIdFactory builder = new MockMessageIdBuilder(baseDir, "serial");

		for (int i = 0; i < 100; i++) {
			Assert.assertEquals(String.format("serial-c0a81f9e-403215-%s", i), builder.getNextId());
		}
	}

	public static class MockApplication {
		private File m_baseDir = new File("target/mark");

		private String buildCommand() {
			List<String> args = new ArrayList<String>();

			args.add("java");
			args.add("-cp");
			args.add(System.getProperty("java.class.path"));
			args.add(MessageIdFactoryTest.class.getName());
			args.add("slave");

			return Joiners.by(' ').join(args);
		}

		public void handleMaster(int size) throws Exception {
			Files.forDir().delete(new File(m_baseDir, "multiple.mark"));

			final AtomicBoolean enabled = new AtomicBoolean(true);
			final ConcurrentMap<String, String> set = new ConcurrentHashMap<String, String>(1024);
			ExecutorService pool = Threads.forPool().getFixedThreadPool("cat", size);
			final List<Process> processes = new ArrayList<Process>();

			for (int i = 0; i < size; i++) {
				String command = buildCommand();
				Process process = Runtime.getRuntime().exec(command);

				processes.add(process);
			}

			for (int i = 0; i < size; i++) {
				final InputStream in = processes.get(i).getInputStream();

				pool.submit(new Runnable() {
					@Override
					public void run() {
						try {
							BufferedReader reader = new BufferedReader(new InputStreamReader(in));

							while (true) {
								String line = reader.readLine();

								if (line == null) {
									break;
								} else if (set.containsKey(line)) {
									System.out.println("Message ID conflicting found: " + line);
								} else {
									set.put(line, line);

									if (set.size() % 50000 == 0) {
										System.out.println("size:" + set.size());
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}

			System.out.println("Press any key to stop ...");
			System.in.read();

			for (Process process : processes) {
				process.getOutputStream().close();
			}

			enabled.set(false);
			pool.shutdown();
			pool.awaitTermination(100, TimeUnit.MILLISECONDS);
		}

		public void handleSlave() throws Exception {
			int threads = 10;
			final MockMessageIdBuilder builder = new MockMessageIdBuilder(m_baseDir, "multiple");
			final AtomicBoolean enabled = new AtomicBoolean(true);
			ExecutorService pool = Threads.forPool().getFixedThreadPool("cat", threads);

			for (int i = 0; i < threads; i++) {
				pool.submit(new Runnable() {
					@Override
					public void run() {
						try {
							while (enabled.get()) {
								System.out.println(builder.getNextId());

								TimeUnit.MILLISECONDS.sleep(1);
							}
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
				});
			}

			System.in.read();

			pool.shutdown();
			pool.awaitTermination(100, TimeUnit.MILLISECONDS);
		}
	}

	private static class MockMessageIdBuilder extends MessageIdFactory {
		private MockMessageIdBuilder(File baseDir, String domain) throws IOException {
			super.initialize(baseDir, domain);
		}

		@Override
		protected int getBatchSize() {
			return 10;
		}

		@Override
		protected String getIpAddress() {
			return "c0a81f9e";
		}

		@Override
		protected long getTimestamp() {
			return 403215;
		}
	}
}
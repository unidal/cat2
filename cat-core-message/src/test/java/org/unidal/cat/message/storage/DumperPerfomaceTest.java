package org.unidal.cat.message.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.CatConstant;
import org.unidal.helper.Files;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class DumperPerfomaceTest extends ComponentTestCase {
	private MessageCodec m_codec;

	@Before
	public void before() {
		File baseDir = new File("target");
		Files.forDir().delete(new File(baseDir, "dump"), true);

		lookup(StorageConfiguration.class).setBaseDataDir(baseDir);
		m_codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
		System.setProperty("devMode", "true");
	}
	
	@Test
	public void testWithIndex(){
		long start = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			for (int domainIndex = 0; domainIndex < 255; domainIndex++) {
				String domain = "domain_" + domainIndex;
				int hour = 405746;

				for (int ipIndex = 0; ipIndex < 10; ipIndex++) {
					String ip = "0a01" + getHex(ipIndex) + getHex(domainIndex);
					MessageId id = new MessageId(domain, ip, hour, i * 10 + ipIndex);
				
					getIndex(id.getDomain());
				}
			}
		}
		
		System.out.println("domain hash duration:" + (System.currentTimeMillis()-start));
		
		 start = System.currentTimeMillis();
		
		for (int i = 0; i < 100000; i++) {
			for (int domainIndex = 0; domainIndex < 255; domainIndex++) {
				String domain = "domain_" + domainIndex;
				int hour = 405746;

				for (int ipIndex = 0; ipIndex < 10; ipIndex++) {
					String ip = "0a01" + getHex(ipIndex) + getHex(domainIndex);
					MessageId id = new MessageId(domain, ip, hour, i * 10 + ipIndex);
				
					getIndex(id.getIpAddressInHex());
				}
			}
		}
		System.out.println("index hash duration:" + (System.currentTimeMillis()-start));
		
	}

	private int getIndex(String key) {
		return (Math.abs(key.hashCode())) % (12);
	}
	
	private String getHex(int index) {
		String s = Integer.toHexString(index);

		if (s.length() == 1) {
			return '0' + s;
		} else {
			return s;
		}
	}

	@Test
	public void testWithManyThreads() {
		TreeHelper.init(m_codec);
		long start = System.currentTimeMillis();
		List<Dumper> threads = new ArrayList<Dumper>();

		for (int i = 0; i < 2; i++) {
			Dumper task = new Dumper(i);

			Threads.forGroup(CatConstant.CAT).start(task);
			threads.add(task);
		}

		while (true) {
			try {
				Thread.sleep(10000);

				long total = 0;

				for (Dumper thread : threads) {
					total = total + thread.m_totalCount.get();
				}
				long duration = System.currentTimeMillis() - start;
				System.out.println("qps:" + total * 1000 / duration);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public class Dumper implements Task {

		public AtomicInteger m_totalCount = new AtomicInteger(0);

		public int m_threadIndex;

		public Dumper(int index) {
			m_threadIndex = index;
		}

		@Override
		public String getName() {
			return "task";
		}

		@Override
		public void run() {
			MessageDumperManager manager = lookup(MessageDumperManager.class);
			int hour = 405746;
			MessageDumper dumper = manager.findOrCreate(hour);

			for (int i = 0; i < 10000000; i++) {
				for (int domainIndex = 0; domainIndex < 255; domainIndex++) {
					String domain = "domain_" + domainIndex;

					for (int ipIndex = 0; ipIndex < 10; ipIndex++) {
						String ip = "0a01" + getHex(ipIndex) + getHex(domainIndex);
						MessageId id = new MessageId(domain, ip, hour, i * 10 + ipIndex);
						MessageTree tree = TreeHelper.cacheTree(m_codec, id);
						
						((DefaultMessageTree) tree).setMessageId(id.toString());
						tree.setFormatMessageId(id);

						dumper.process(tree);
						m_totalCount.addAndGet(1);
					}
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}
	
}

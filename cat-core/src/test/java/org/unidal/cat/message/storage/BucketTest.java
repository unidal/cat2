package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.message.MessageId;
import org.unidal.cat.message.storage.internals.DefaultBlock;
import org.unidal.cat.metric.Benchmark;
import org.unidal.cat.metric.BenchmarkManager;
import org.unidal.cat.metric.Metric;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

public class BucketTest extends ComponentTestCase {
	private MessageCodec m_codec;

	private BenchmarkManager m_benchmarkManager;

	@Before
	public void before() {
		m_codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
		m_benchmarkManager = lookup(BenchmarkManager.class);
		lookup(StorageConfiguration.class).setBaseDataDir(new File("target"));
	}

	@Test
	public void testWriteAndRead() throws Exception {
		BucketManager manager = lookup(BucketManager.class, "local");

		for (int i = 0; i < 1; i++) {
			String domain = "mock";
			int hour = 404857;
			Bucket bucket = manager.getBucket(domain, hour, true);
			Block block = new DefaultBlock(domain, hour);

			for (int count = 0; count < 10; count++) {
				MessageId id = new MessageId(domain, "0a010203", hour, i);
				MessageTree tree = TreeHelper.tree(m_codec, id);

				block.pack(id, tree);
			}

			try {
				bucket.put(block);
			} catch (Exception e) {
				System.out.println(i);
				throw e;
			}

			for (MessageId id : block.getMappings().keySet()) {
				Block b = bucket.get(id);
				MessageTree tree = b.findTree(id);

				Assert.assertEquals(id.toString(), tree.getMessageId());
			}
		}
	}

	@Test
	public void testWritePerformance() throws IOException {
		BucketManager manager = lookup(BucketManager.class, "local");
		Benchmark bm = m_benchmarkManager.get("bucket");
		Metric mb = bm.get("build");
		Metric mo = bm.get("other");
		Metric mc = bm.get("close");

		for (int i = 0; i < 1000000; i++) {
			mo.start();

			String domain = "mock";
			Bucket bucket = manager.getBucket(domain, 404448, true);

			mo.end();

			mb.start();
			Block block = new MockBlock(domain, 404448, 10, i);
			mb.end();

			mo.start();
			try {
				bucket.put(block);
			} catch (Exception e) {
				System.out.println(i);
				e.printStackTrace();
				break;
			}
			mo.end();
		}

		mc.start();
		manager.closeBuckets();
		mc.end();

		bm.print();
	}

	private static class MockBlock implements Block {
		private String m_domain;

		private int m_hour;

		private int m_capacity = 1536;

		private Map<MessageId, Integer> m_mappings = new HashMap<MessageId, Integer>();

		public MockBlock(String domain, int hour, int count, int index) {
			m_domain = domain;
			m_hour = hour;

			String ip = "10.1.2.4";

			for (int i = 0; i < count; i++) {
				MessageId id = new MessageId(domain, ip, hour, count * index + i);

				m_mappings.put(id, i % m_capacity);
			}
		}

		@Override
		public MessageTree findTree(MessageId messageId) {
			return null;
		}

		@Override
		public void finish() {
		}

		@Override
		public ByteBuf getData() throws IOException {
			ByteBuf buf = Unpooled.buffer(m_capacity);

			buf.writeZero(m_capacity);
			return buf;
		}

		@Override
		public String getDomain() {
			return m_domain;
		}

		@Override
		public int getHour() {
			return m_hour;
		}

		@Override
		public Map<MessageId, Integer> getMappings() {
			return m_mappings;
		}

		@Override
		public boolean isFull() {
			return true;
		}

		@Override
		public void pack(MessageId id, MessageTree tree) throws IOException {
		}

		@Override
		public ByteBuf unpack(MessageId id) throws IOException {
			throw new UnsupportedOperationException();
		}
	}
}

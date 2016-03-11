package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.message.MessageId;
import org.unidal.cat.metric.Benchmark;
import org.unidal.cat.metric.BenchmarkManager;
import org.unidal.cat.metric.Metric;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

public class MessageDumperTest extends ComponentTestCase {
	private MessageCodec m_codec;

	private BenchmarkManager m_benchmarkManager;

	@Before
	public void before() {
		m_codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
		m_benchmarkManager = lookup(BenchmarkManager.class);
		lookup(StorageConfiguration.class).setBaseDataDir(new File("target"));
	}

	@Test
	public void testRead() throws Exception {
		BucketManager manager = lookup(BucketManager.class, "local");
		Benchmark sw = m_benchmarkManager.get("test");
		Metric m = sw.get("message");

		for (int i = 0; i < 100000; i++) {
			Bucket bucket = manager.getBucket("mock", 404259, true);
			MessageId id = new MessageId("mock", "0a010203", 404259, i);

			try {
				Block block = bucket.get(id);

				m.start();
				ByteBuf buf = block.unpack(id);
				MessageTree tree = m_codec.decode(buf);
				m.end();

				Assert.assertEquals(id.toString(), tree.getMessageId());
			} catch (Exception e) {
				throw new Exception(String.format("Error when loading message(%s)! ", id), e);
			}
		}

		sw.print();
	}

	@Test
	public void testWrite() throws Exception {
		MessageDumper dumper = lookup(MessageDumper.class);
		Benchmark sw = m_benchmarkManager.get("test");
		Metric m = sw.get("message");
		Metric p = sw.get("dump");

		for (int i = 0; i < 100000; i++) {
			m.start();

			MessageTree tree = TreeHelper.tree(m_codec, new MessageId("mock", "0a010203", 404259, i));

			m.end();

			p.start();
			dumper.process(tree);
			p.end();
		}

		p.start();
		dumper.awaitTermination();
		p.end();

		sw.print();
	}

	@Test
	public void testReadWrite() throws Exception {
		MessageDumper dumper = lookup(MessageDumper.class);

		for (int i = 0; i < 100000; i++) {
			String mid = "mock2-0a010203-404259-" + i;
			MessageTree tree = TreeHelper.tree(m_codec, mid);

			dumper.process(tree);
		}

		dumper.awaitTermination();
	}
}

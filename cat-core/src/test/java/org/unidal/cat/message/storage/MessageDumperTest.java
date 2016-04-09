package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.message.TreeHelper;
import org.unidal.cat.metric.Benchmark;
import org.unidal.cat.metric.BenchmarkManager;
import org.unidal.cat.metric.Metric;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

public class MessageDumperTest extends ComponentTestCase {
	private MessageCodec m_codec;

	private BenchmarkManager m_benchmarkManager;

	@Before
	public void before() {
		File baseDir = new File("target");

		Files.forDir().delete(new File(baseDir, "dump"), true);

		lookup(StorageConfiguration.class).setBaseDataDir(baseDir);
		m_codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
		m_benchmarkManager = lookup(BenchmarkManager.class);
	}

	@Test
	public void testRead() throws Exception {
		BucketManager manager = lookup(BucketManager.class, "local");
		Benchmark sw = m_benchmarkManager.get("test");
		Metric m = sw.get("message");
		String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

		for (int i = 0; i < 100000; i++) {
			Bucket bucket = manager.getBucket("mock", ip, 404259, true);
			MessageId id = new MessageId("mock", ip, 404259, i);

			try {
				ByteBuf buf = bucket.get(id);

				m.start();
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
		Benchmark sw = m_benchmarkManager.get("write");
		Metric m = sw.get("message");
		Metric p = sw.get("dump");

		for (int i = 0; i < 100000; i++) {
			m.start();

			MessageId id = new MessageId("mock", "0a010203", 404259, i);
			MessageTree tree = TreeHelper.tree(m_codec, id);

			m.end();

			p.start();
			dumper.process(tree);
			p.end();
		}

		p.start();
		dumper.awaitTermination(404259);
		p.end();

		sw.print();
	}
}

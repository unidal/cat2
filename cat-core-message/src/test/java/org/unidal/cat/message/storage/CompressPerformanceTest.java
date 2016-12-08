package org.unidal.cat.message.storage;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.message.storage.internals.CompressType;
import org.unidal.cat.message.storage.internals.DefaultBlock;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

public class CompressPerformanceTest extends ComponentTestCase {
	private MessageCodec m_codec;
	
	private int m_total = 20000;

	@Before
	public void before() {
		File baseDir = new File("target");

		Files.forDir().delete(new File(baseDir, "dump"), true);

		lookup(StorageConfiguration.class).setBaseDataDir(baseDir);
		m_codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
		System.setProperty("devMode", "true");
		TreeHelper.init(m_codec);
	}

	@Test
	public void testGzip() throws Exception {
		String ip = "0a010203";
		String domain = "mock";
		int hour = 404857;

		long start = System.currentTimeMillis();
		long size = 0;
		for (int i = 0; i < m_total; i++) {
			Block block = new DefaultBlock(domain, hour, CompressType.GZIP);

			for (int index = 0; index < 500; index++) {
				MessageId id = new MessageId(domain, ip, hour, i * 10 + index);
				MessageTree tree = TreeHelper.cacheTree(m_codec, id);

				block.pack(id, tree.getBuffer());
			}
			block.finish();
			size = size + block.getData().readableBytes();
		}
		long duration = System.currentTimeMillis() - start;
		System.out.println("gzip:" + duration + " size:" + size);
	}

	@Test
	public void testDeflate() throws Exception {
		String ip = "0a010203";
		String domain = "mock";
		int hour = 404857;

		long start = System.currentTimeMillis();
		long size = 0;
		for (int i = 0; i < m_total; i++) {
			Block block = new DefaultBlock(domain, hour, CompressType.DEFLATE);

			for (int index = 0; index < 500; index++) {
				MessageId id = new MessageId(domain, ip, hour, i * 10 + index);
				MessageTree tree = TreeHelper.cacheTree(m_codec, id);

				block.pack(id, tree.getBuffer());
			}
			block.finish();
			int readableBytes = block.getData().readableBytes();
			size = size + readableBytes;
		}
		long duration = System.currentTimeMillis() - start;
		System.out.println("delfate:" + duration + " size:" + size);
	}

	@Test
	public void testSnappy() throws Exception {
		String ip = "0a010203";
		String domain = "mock";
		int hour = 404857;

		long start = System.currentTimeMillis();
		long size = 0;
		for (int i = 0; i < m_total; i++) {
			Block block = new DefaultBlock(domain, hour, CompressType.SNAPPY);

			for (int index = 0; index < 500; index++) {
				MessageId id = new MessageId(domain, ip, hour, i * 10 + index);
				MessageTree tree = TreeHelper.cacheTree(m_codec, id);

				block.pack(id, tree.getBuffer());
			}
			block.finish();
			int readableBytes = block.getData().readableBytes();
			size = size + readableBytes;
		}
		long duration = System.currentTimeMillis() - start;
		System.out.println("snappy:" + duration + " size:" + size);
	}

}

package org.unidal.cat.message.storage.hdfs;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.message.storage.Index;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.internal.MessageId;

public class HdfsIndexManagerTest extends ComponentTestCase {

	@Before
	public void before() throws Exception {
		ServerConfigManager config = lookup(ServerConfigManager.class);

		config.initialize(new File(HdfsBucketTest.class.getClassLoader().getResource("server.xml").getFile()));
	}

	@Test
	public void testManager() {
		HdfsIndexManager manager = lookup(HdfsIndexManager.class);

		for (int i = 0; i < 1000; i++) {
			MessageId id = MessageId.parse("cat-0a420d73-405915-" + i);

			MessageId message = manager.loadMessage(id);

			if (message != null) {
				System.err.println(message);
			}
		}
	}

	@Test
	public void test() {
		Index index = lookup(Index.class, "hdfs");
		try {
			for (int i = 0; i < 100; i++) {
				MessageId id = MessageId.parse("shop-web-0a420d56-405915-" + i);
				index.initialize(id.getDomain(), "10.66.13.115", id.getHour());
				MessageId to = index.find(id);

				System.out.println(to);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

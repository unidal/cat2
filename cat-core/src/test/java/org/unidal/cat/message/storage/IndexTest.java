package org.unidal.cat.message.storage;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.internal.MessageId;

public class IndexTest extends ComponentTestCase {
	@Before
	public void before() throws Exception {
		StorageConfiguration config = lookup(StorageConfiguration.class);

		config.setBaseDataDir(new File("target"));
		File baseDir = new File("target");

		Files.forDir().delete(new File(baseDir, "dump"), true);
	}

	@Test
	public void testMapAndLookups() throws Exception {
		IndexManager manager = lookup(IndexManager.class, "local");
		Index index = manager.getIndex("from", 403899, true);

		for (int i = 1; i < 150000; i++) {
			MessageId from = MessageId.parse("from-0a260014-403899-" + i);
			MessageId to = MessageId.parse("to-0a260015-403899-" + i);

			index.map(from, to);
		}

		index.close();
		index = manager.getIndex("from", 403899, true);
		for (int i = 1; i < 150000; i++) {
			MessageId from = MessageId.parse("from-0a260014-403899-" + i);
			MessageId expected = MessageId.parse("to-0a260015-403899-" + i);

			MessageId actual = index.lookup(from);

			Assert.assertEquals(expected, actual);
		}
	}

	@Test
	public void testMapAndLookupManyIps() throws Exception {
		IndexManager manager = lookup(IndexManager.class, "local");
		Index index = manager.getIndex("from", 403899, true);

		for (int i = 1; i < 150000; i++) {
			for (int ip = 0; ip < 9; ip++) {
				MessageId from = MessageId.parse("from-0a26000" + ip + "-403899-" + i);
				MessageId to = MessageId.parse("from-0a25000" + ip + "-403899-" + i);

				index.map(from, to);
			}

		}

		index.close();
		index = manager.getIndex("from", 403899, true);
		for (int i = 1; i < 150000; i++) {

			for (int ip = 0; ip < 9; ip++) {
				MessageId from = MessageId.parse("from-0a26000" + ip + "-403899-" + i);
				MessageId expected = MessageId.parse("from-0a25000" + ip + "-403899-" + i);

				MessageId actual = index.lookup(from);

				Assert.assertEquals(expected, actual);
			}
		}
	}

}

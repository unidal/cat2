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
	public void testMapAndLookup() throws Exception {
		MessageId from = MessageId.parse("from-0a260014-403899-76543");
		MessageId expected = MessageId.parse("to-0a260015-403899-12345");
		IndexManager manager = lookup(IndexManager.class, "local");
		Index index = manager.getIndex(from.getDomain(), from.getHour(), true);

		index.map(from, expected);

		MessageId actual = index.lookup(from);

		try {
			Assert.assertEquals(expected, actual);
		} finally {
			index.close();
		}
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
}

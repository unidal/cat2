package org.unidal.cat.message.storage;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.message.storage.Index;
import org.unidal.cat.message.storage.IndexManager;
import org.unidal.cat.message.storage.MessageId;
import org.unidal.cat.message.storage.StorageConfiguration;
import org.unidal.lookup.ComponentTestCase;

public class IndexTest extends ComponentTestCase {
	@Test
	public void testMapAndLookup() throws Exception {
		StorageConfiguration config = lookup(StorageConfiguration.class);

		config.setBaseDataDir(new File("target"));

		MessageId from = MessageId.parse("from-0a260014-403899-76543");
		MessageId expected = MessageId.parse("to-0a260015-403899-12345");
		IndexManager manager = lookup(IndexManager.class, "local");
		Index index = manager.getIndex(from, true);

		index.map(from, expected);

		MessageId actual = index.lookup(from);

		try {
			Assert.assertEquals(expected, actual);
		} finally {
			index.close();
		}
	}
}

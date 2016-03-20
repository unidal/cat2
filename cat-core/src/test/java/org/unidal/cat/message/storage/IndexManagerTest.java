package org.unidal.cat.message.storage;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.internal.MessageId;

public class IndexManagerTest extends ComponentTestCase {
	@Test
	public void test() throws IOException {
		IndexManager manager = lookup(IndexManager.class, "local");
		MessageId id = MessageId.parse("mock-0a260014-403890-12345");

		Assert.assertNull(manager.getIndex(id, false));
		Assert.assertNotNull(manager.getIndex(id, true));
	}
}

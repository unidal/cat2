package org.unidal.cat.message.storage;

import java.io.IOException;

import org.unidal.cat.message.MessageId;

public interface Bucket {
	public void close();

	public Block get(MessageId id) throws IOException;

	public void put(Block block) throws IOException;
}

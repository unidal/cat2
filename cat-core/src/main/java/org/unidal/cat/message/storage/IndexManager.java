package org.unidal.cat.message.storage;

import java.io.IOException;

import org.unidal.cat.message.MessageId;

public interface IndexManager {
	public Index getIndex(MessageId from, boolean createIfNotExists) throws IOException;
}

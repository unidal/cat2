package org.unidal.cat.message.storage;

import java.io.IOException;

public interface IndexManager {
	public Index getIndex(MessageId from, boolean createIfNotExists) throws IOException;
}

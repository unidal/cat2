package org.unidal.cat.message.storage;

import java.io.IOException;

public interface IndexManager {
	public Index getIndex(String domain, int hour, boolean createIfNotExists) throws IOException;
}

package org.unidal.cat.message.storage;

import java.io.IOException;

import com.dianping.cat.message.internal.MessageId;

public interface IndexManager {
	public Index getIndex(MessageId from, boolean createIfNotExists) throws IOException;
}

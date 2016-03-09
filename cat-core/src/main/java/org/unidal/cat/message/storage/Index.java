package org.unidal.cat.message.storage;

import java.io.IOException;

import org.unidal.cat.message.MessageId;

public interface Index {
	public void close();

	public MessageId lookup(MessageId from) throws IOException;

	public void map(MessageId from, MessageId to) throws IOException;
}

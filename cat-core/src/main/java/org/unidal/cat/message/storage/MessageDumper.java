package org.unidal.cat.message.storage;

import org.unidal.cat.message.MessageId;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageDumper {
	public void awaitTermination() throws InterruptedException;

	public MessageTree find(MessageId id);

	public void process(MessageTree tree);
}

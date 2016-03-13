package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;

import org.unidal.cat.message.MessageId;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageDumper {
	public void awaitTermination() throws InterruptedException;

	public void process(MessageTree tree);

	public ByteBuf find(MessageId id);
}

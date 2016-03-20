package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;

import org.unidal.cat.message.MessageId;

public interface MessageFinder {
	public ByteBuf find(MessageId id);
}

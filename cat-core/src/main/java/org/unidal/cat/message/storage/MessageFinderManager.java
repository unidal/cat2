package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;

import org.unidal.cat.message.MessageId;

public interface MessageFinderManager {
	public void close(int hour);

	public ByteBuf find(MessageId id);
	
	public void register(int hour, MessageFinder finder);
}

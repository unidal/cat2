package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Map;

import org.unidal.cat.message.MessageId;

import com.dianping.cat.message.spi.MessageTree;

public interface Block {
	public MessageTree findTree(MessageId messageId);

	public void finish();

	public ByteBuf getData() throws IOException;

	public String getDomain();

	public int getHour();

	public Map<MessageId, Integer> getMappings();

	public boolean isFull();

	public void pack(MessageId id, MessageTree tree) throws IOException;

	public ByteBuf unpack(MessageId id) throws IOException;
}

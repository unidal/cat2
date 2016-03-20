package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.BlockingQueue;

import org.unidal.cat.message.MessageId;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageProcessor extends Task {
	public ByteBuf findTree(MessageId messageId);

	public void initialize(long timestamp, int index, BlockingQueue<MessageTree> queue);

}

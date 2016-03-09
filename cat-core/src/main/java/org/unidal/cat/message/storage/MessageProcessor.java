package org.unidal.cat.message.storage;

import java.util.concurrent.BlockingQueue;

import org.unidal.cat.message.MessageId;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageProcessor extends Task {
	
	public MessageTree findTree(MessageId id);

	public void initialize(int index, BlockingQueue<MessageTree> queue);
	
}

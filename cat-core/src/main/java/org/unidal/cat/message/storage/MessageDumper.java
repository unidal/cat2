package org.unidal.cat.message.storage;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageDumper {
	public void doCheckpoint(boolean atEnd);

	public void process(MessageTree tree);
}

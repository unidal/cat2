package org.unidal.cat.spi.analysis;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageDispatcher {
	public void dispatch(MessageTree tree);
}

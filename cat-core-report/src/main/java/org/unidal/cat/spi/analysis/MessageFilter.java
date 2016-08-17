package org.unidal.cat.spi.analysis;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageFilter {
	public boolean apply(MessageTree tree);
}

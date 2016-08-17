package org.unidal.cat.spi.analysis;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageRoutingStrategy {
    public int getIndex(MessageTree tree, int size);
}

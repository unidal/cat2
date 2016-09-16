package org.unidal.cat.core.message.service;

import java.io.IOException;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;

public interface MessageService {
   public MessageTree getMessageTree(MessageId id) throws IOException;
}

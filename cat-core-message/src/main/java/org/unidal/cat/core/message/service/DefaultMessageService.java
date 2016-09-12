package org.unidal.cat.core.message.service;

import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageService.class)
public class DefaultMessageService implements MessageService {
   @Override
   public MessageTree getMessageTree(MessageId id) {
      return null;
   }
}

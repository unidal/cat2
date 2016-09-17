package org.unidal.cat.core.message.provider;

import java.io.IOException;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageProvider {
   public boolean isEligible(MessageContext ctx);

   public MessageTree getMessage(MessageContext ctx) throws IOException;
}

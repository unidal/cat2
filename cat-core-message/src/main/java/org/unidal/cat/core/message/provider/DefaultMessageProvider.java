package org.unidal.cat.core.message.provider;

import java.io.IOException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageProvider.class)
public class DefaultMessageProvider implements MessageProvider {
   @Inject(RecentMessageProvider.ID)
   private MessageProvider m_recent;

   @Inject(HistoricalMessageProvider.ID)
   private MessageProvider m_historical;

   @Override
   public boolean isEligible(MessageContext ctx) {
      return true;
   }

   @Override
   public MessageTree getMessage(MessageContext ctx) throws IOException {
      if (m_historical.isEligible(ctx)) {
         MessageTree tree = m_historical.getMessage(ctx);

         if (tree != null) {
            return tree;
         } else {
            // fallback to local
         }
      }

      return m_recent.getMessage(ctx);
   }
}

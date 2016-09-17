package org.unidal.cat.core.message.provider;

import java.io.IOException;

import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageProvider.class, value = HistoricalMessageProvider.ID)
public class HistoricalMessageProvider implements MessageProvider {
   public static final String ID = "historical";

   @Override
   public MessageTree getMessage(MessageContext ctx) throws IOException {
      Transaction t = Cat.getProducer().newTransaction("LogTree", "Historical");

      t.addData(ctx.toString());

      try {
         // TODO use HDFS web access here
         t.setStatus(Message.SUCCESS);
         return null;
      } catch (RuntimeException e) {
         Cat.logError(e);
         t.setStatus(e);
         throw e;
      } catch (Error e) {
         Cat.logError(e);
         t.setStatus(e);
         throw e;
      } finally {
         t.complete();
      }
   }

   @Override
   public boolean isEligible(MessageContext ctx) {
      return !ctx.isLocal();
   }
}

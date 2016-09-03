package org.unidal.cat.plugin.transaction;

import java.util.List;

import org.unidal.cat.plugin.transaction.model.entity.Duration;
import org.unidal.cat.plugin.transaction.model.entity.Range;
import org.unidal.cat.plugin.transaction.model.entity.TransactionName;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.model.entity.TransactionType;
import org.unidal.cat.spi.analysis.AbstractMessageAnalyzer;
import org.unidal.cat.spi.analysis.MessageAnalyzer;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageAnalyzer.class, value = TransactionConstants.NAME, instantiationStrategy = Named.PER_LOOKUP)
public class TransactionReportAnalyzer extends AbstractMessageAnalyzer<TransactionReport> {
   @Inject
   private TransactionConfigService m_configService;

   private Pair<Boolean, Long> checkForTruncatedMessage(MessageTree tree, Transaction t) {
      Pair<Boolean, Long> pair = new Pair<Boolean, Long>(true, t.getDurationInMicros());
      List<Message> children = t.getChildren();
      int size = children.size();

      // root transaction with children
      if (tree.getMessage() == t && size > 0) {
         Message last = children.get(size - 1);

         if (last instanceof Event) {
            String type = last.getType();
            String name = last.getName();

            if (type.equals("RemoteCall") && name.equals("Next")) {
               pair.setKey(false);
            } else if (type.equals("TruncatedTransaction") && name.equals("TotalDuration")) {
               try {
                  long delta = Long.parseLong(last.getData().toString());

                  pair.setValue(delta);
               } catch (Exception e) {
                  Cat.logError(e);
               }
            }
         }
      }

      return pair;
   }

   @Override
   public void process(MessageTree tree) {
      Message message = tree.getMessage();

      if (message instanceof Transaction) {
         String domain = tree.getDomain();
         TransactionReport report = getLocalReport(domain);
         Transaction root = (Transaction) message;

         processTransaction(report, tree, root);
      }
   }

   private void processNameGraph(Transaction t, TransactionName name, int min, double d) {
      int dk = 1;

      if (d > 65536) {
         dk = 65536;
      } else {
         if (dk > 256) {
            dk = 256;
         }
         while (dk < d) {
            dk <<= 1;
         }
      }

      Duration duration = name.findOrCreateDuration(dk);
      Range range = name.findOrCreateRange(min);

      duration.incCount();
      range.incCount();

      if (!t.isSuccess()) {
         range.incFails();
      }

      range.setSum(range.getSum() + d);
   }

   private void processTransaction(TransactionReport report, MessageTree tree, Transaction t) {
      if (m_configService.isEligible(tree.getDomain())) {
         Pair<Boolean, Long> pair = checkForTruncatedMessage(tree, t);

         report.addIp(tree.getIpAddress());

         if (pair.getKey().booleanValue()) {
            String ip = tree.getIpAddress();
            TransactionType transactionType = report.findOrCreateMachine(ip).findOrCreateType(t.getType());
            TransactionName transactionName = transactionType.findOrCreateName(t.getName());
            String messageId = tree.getMessageId();

            processTypeAndName(t, transactionType, transactionName, messageId, pair.getValue().doubleValue() / 1000d);
         }

         List<Message> children = t.getChildren();

         for (Message child : children) {
            if (child instanceof Transaction) {
               processTransaction(report, tree, (Transaction) child);
            }
         }
      }
   }

   private void processTypeAndName(Transaction t, TransactionType type, TransactionName name, String messageId,
         double duration) {
      type.incTotalCount();
      name.incTotalCount();

      if (t.isSuccess()) {
         if (type.getSuccessMessageUrl() == null) {
            type.setSuccessMessageUrl(messageId);
         }

         if (name.getSuccessMessageUrl() == null) {
            name.setSuccessMessageUrl(messageId);
         }
      } else {
         type.incFailCount();
         name.incFailCount();

         if (type.getFailMessageUrl() == null) {
            type.setFailMessageUrl(messageId);
         }

         if (name.getFailMessageUrl() == null) {
            name.setFailMessageUrl(messageId);
         }
      }

      if (type.getMax() < duration) {
         type.setSlowestMessageUrl(messageId);
      }
      if (name.getMax() < duration) {
         name.setSlowestMessageUrl(messageId);
      }

      double duration2 = duration * duration;

      name.setMax(Math.max(name.getMax(), duration));
      name.setMin(Math.min(name.getMin(), duration));
      name.setSum(name.getSum() + duration);
      name.setSum2(name.getSum2() + duration2);

      type.setMax(Math.max(type.getMax(), duration));
      type.setMin(Math.min(type.getMin(), duration));
      type.setSum(type.getSum() + duration);
      type.setSum2(type.getSum2() + duration2);

      long current = t.getTimestamp() / 1000 / 60;
      int min = (int) (current % 60);

      processNameGraph(t, name, min, duration);
   }
}

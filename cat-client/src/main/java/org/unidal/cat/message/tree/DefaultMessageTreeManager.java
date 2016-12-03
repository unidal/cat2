package org.unidal.cat.message.tree;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.config.ClientConfigurationManager;
import org.unidal.cat.config.ClientEnvironmentSettings;
import org.unidal.cat.message.MessageIdFactory;
import org.unidal.cat.message.MessagePolicy;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.TaggedTransaction;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultForkedTransaction;
import com.dianping.cat.message.internal.DefaultTaggedTransaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.io.MessageSender;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@Named(type = MessageTreeManager.class)
public class DefaultMessageTreeManager extends ContainerHolder implements MessageTreeManager, Initializable, LogEnabled {
   @Inject
   private ClientEnvironmentSettings m_settings;

   @Inject
   private MessagePolicy m_policy;

   @Inject
   private ClientConfigurationManager m_configManager;

   @Inject
   private TransportManager m_transportManager;

   @Inject
   private MessageIdFactory m_factory;

   // we don't use static modifier since MessageManager is configured as singleton
   private ThreadLocal<Context> m_context = new ThreadLocal<Context>() {
      @Override
      protected Context initialValue() {
         if (m_policy.isEnabled()) {
            return new Context();
         } else {
            return null;
         }
      }
   };

   private long m_throttleTimes;

   private boolean m_firstMessage = true;

   private TransactionHelper m_helper = new TransactionHelper();

   private Map<String, TaggedTransaction> m_taggedTransactions;

   private Logger m_logger;

   @Override
   public void add(Message message) {
      Context ctx = m_context.get();

      if (ctx != null) {
         ctx.add(message);
      }
   }

   @Override
   public void bind(String tag, String title) {
      TaggedTransaction t = m_taggedTransactions.get(tag);

      if (t != null) {
         MessageTree tree = getThreadLocalMessageTree();
         String messageId = tree.getMessageId();

         if (messageId == null) {
            messageId = m_factory.getNextId();
            tree.setMessageId(messageId);
         }
         if (tree != null) {
            t.start();
            t.bind(tag, messageId, title);
         }
      }
   }

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   @Override
   public void end(Transaction transaction) {
      Context ctx = m_context.get();

      if (ctx != null && transaction.isStandalone()) {
         if (ctx.end(this, transaction)) {
            m_context.remove();
         }
      }
   }

   private void flush(MessageTree tree) {
      if (tree.getMessageId() == null) {
         tree.setMessageId(m_factory.getNextId());
      }

      MessageSender sender = m_transportManager.getSender();

      if (sender != null && m_policy.isEnabled()) {
         sender.send(tree);

         reset();
      } else {
         m_throttleTimes++;

         if (m_throttleTimes % 10000 == 0 || m_throttleTimes == 1) {
            m_logger.info("Cat Message is throttled! Times:" + m_throttleTimes);
         }
      }
   }

   @Override
   public Transaction getPeekTransaction() {
      Context ctx = m_context.get();

      if (ctx != null) {
         return ctx.peekTransaction();
      } else {
         return null;
      }
   }

   @Override
   public MessageTree getThreadLocalMessageTree() {
      Context ctx = m_context.get();

      if (ctx != null) {
         return ctx.m_tree;
      } else {
         return null;
      }
   }

   @Override
   public boolean hasContext() {
      Context ctx = m_context.get();

      return ctx != null && !ctx.isEmpty();
   }

   @Override
   public void initialize() throws InitializationException {
      // initialize the tagged transaction cache
      final int size = m_configManager.getConfig().getTaggedTransactionCacheSize();

      m_taggedTransactions = new LinkedHashMap<String, TaggedTransaction>(size * 4 / 3 + 1, 0.75f, true) {
         private static final long serialVersionUID = 1L;

         @Override
         protected boolean removeEldestEntry(Entry<String, TaggedTransaction> eldest) {
            return size() >= size;
         }
      };
   }

   public boolean isTraceMode() {
      Context ctx = m_context.get();

      return ctx != null && ctx.isTraceMode();
   }

   public void linkAsRunAway(DefaultForkedTransaction transaction) {
      Context ctx = m_context.get();

      if (ctx != null) {
         ctx.linkAsRunAway(transaction);
      }
   }

   @Override
   public void reset() {
      // destroy current thread local data
      Context ctx = m_context.get();

      if (ctx != null) {
         if (ctx.m_totalDurationInMicros == 0) {
            ctx.m_stack.clear();
            ctx.m_knownExceptions.clear();
            m_context.remove();
         } else {
            ctx.m_knownExceptions.clear();
            // TODO why NOT remove context?
         }
      }
   }

   public void setTraceMode(boolean traceMode) {
      Context ctx = m_context.get();

      if (ctx != null) {
         ctx.setTraceMode(traceMode);
      }
   }

   @Override
   public void setup() {
      // m_context.set(new Context());
   }

   boolean shouldLog(Throwable e) {
      Context ctx = m_context.get();

      if (ctx != null) {
         return ctx.shouldLog(e);
      } else {
         return true;
      }
   }

   @Override
   public void start(Transaction transaction, boolean forked) {
      Context ctx = m_context.get();

      if (ctx != null) {
         ctx.start(transaction, forked);

         if (transaction instanceof TaggedTransaction) {
            TaggedTransaction tt = (TaggedTransaction) transaction;

            m_taggedTransactions.put(tt.getTag(), tt);
         }
      } else if (m_firstMessage) {
         m_firstMessage = false;
         m_logger.warn("CAT client is DISABLED due to not initialized yet!");
      }
   }

   class Context {
      private MessageTree m_tree;

      private Stack<Transaction> m_stack;

      private Set<Throwable> m_knownExceptions;

      private int m_length;

      private boolean m_traceMode;

      private long m_totalDurationInMicros; // for truncate message

      public Context() {
         Thread thread = Thread.currentThread();
         String groupName = thread.getThreadGroup().getName();

         m_tree = new DefaultMessageTree();
         m_tree.setThreadGroupName(groupName);
         m_tree.setThreadId(String.valueOf(thread.getId()));
         m_tree.setThreadName(thread.getName());
         m_tree.setDomain(m_settings.getDomain());
         m_tree.setHostName(m_settings.getHostName());
         m_tree.setIpAddress(m_settings.getIpAddress());
         m_stack = new Stack<Transaction>();
         m_knownExceptions = new HashSet<Throwable>();
         m_length = 1;
      }

      public void add(Message message) {
         if (m_stack.isEmpty()) {
            MessageTree tree = m_tree.copy();

            tree.setMessage(message);
            flush(tree);
         } else {
            Transaction parent = m_stack.peek();

            addTransactionChild(message, parent);
         }
      }

      private void addTransactionChild(Message message, Transaction transaction) {
         long treePeriod = trimToHour(m_tree.getMessage().getTimestamp());
         long messagePeriod = trimToHour(message.getTimestamp() - 10 * 1000L); // 10 seconds extra time allowed

         if (treePeriod < messagePeriod || m_length >= m_configManager.getConfig().getMaxMessageLines()) {
            m_helper.truncateAndFlush(this, message.getTimestamp());
         }

         transaction.addChild(message);
         m_length++;
      }

      private void adjustForTruncatedTransaction(Transaction root) {
         DefaultEvent next = new DefaultEvent("TruncatedTransaction", "TotalDuration");
         long actualDurationInMicros = m_totalDurationInMicros + root.getDurationInMicros();

         next.addData(String.valueOf(actualDurationInMicros));
         next.setStatus(Message.SUCCESS);
         root.addChild(next);

         m_totalDurationInMicros = 0;
      }

      /**
       * return true means the transaction has been flushed.
       * 
       * @param manager
       * @param transaction
       * @return true if message is flushed, false otherwise
       */
      public boolean end(DefaultMessageTreeManager manager, Transaction transaction) {
         if (!m_stack.isEmpty()) {
            Transaction current = m_stack.pop();

            if (transaction == current) {
               m_helper.validate(m_stack.isEmpty() ? null : m_stack.peek(), current);
            } else {
               while (transaction != current && !m_stack.empty()) {
                  m_helper.validate(m_stack.peek(), current);

                  current = m_stack.pop();
               }
            }

            if (m_stack.isEmpty()) {
               MessageTree tree = m_tree.copy();

               m_tree.setMessageId(null);
               m_tree.setMessage(null);

               if (m_totalDurationInMicros > 0) {
                  adjustForTruncatedTransaction((Transaction) tree.getMessage());
               }

               manager.flush(tree);
               return true;
            }
         }

         return false;
      }

      public boolean isTraceMode() {
         return m_traceMode;
      }

      public void linkAsRunAway(DefaultForkedTransaction transaction) {
         m_helper.linkAsRunAway(transaction);
      }

      public Transaction peekTransaction() {
         if (m_stack.size() > 0) {
            return m_stack.peek();
         }

         return null;
      }

      public boolean isEmpty() {
         return m_stack.isEmpty();
      }

      public void setTraceMode(boolean traceMode) {
         m_traceMode = traceMode;
      }

      public boolean shouldLog(Throwable e) {
         if (m_knownExceptions == null) {
            m_knownExceptions = new HashSet<Throwable>();
         }

         if (m_knownExceptions.contains(e)) {
            return false;
         } else {
            m_knownExceptions.add(e);
            return true;
         }
      }

      public void start(Transaction transaction, boolean forked) {
         if (!m_stack.isEmpty()) {
            // Do NOT make strong reference from parent transaction to forked transaction.
            // Instead, we create a "soft" reference to forked transaction later, via linkAsRunAway()
            // By doing so, there is no need for synchronization between parent and child threads.
            // Both threads can call complete() independently.
            if (!(transaction instanceof ForkedTransaction)) {
               Transaction parent = m_stack.peek();

               addTransactionChild(transaction, parent);
            }
         } else {
            m_tree.setMessage(transaction);
         }

         if (!forked) {
            m_stack.push(transaction);
         }
      }

      private long trimToHour(long timestamp) {
         return timestamp - timestamp % (3600 * 1000L);
      }
   }

   class TransactionHelper {
      private void linkAsRunAway(DefaultForkedTransaction transaction) {
         DefaultEvent event = new DefaultEvent("RemoteCall", "RunAway");

         event.addData(transaction.getForkedMessageId(), transaction.getType() + ":" + transaction.getName());
         event.setTimestamp(transaction.getTimestamp());
         event.setStatus(Message.SUCCESS);
         event.setCompleted(true);
         transaction.setStandalone(true);

         add(event);
      }

      private void markAsNotCompleted(DefaultTransaction transaction) {
         DefaultEvent event = new DefaultEvent("cat", "BadInstrument");

         event.setStatus("TransactionNotCompleted");
         event.setCompleted(true);
         transaction.addChild(event);
         transaction.setCompleted(true);
      }

      private void markAsRunAway(Transaction parent, DefaultTaggedTransaction transaction) {
         if (!transaction.hasChildren()) {
            transaction.addData("RunAway");
         }

         transaction.setStatus(Message.SUCCESS);
         transaction.setStandalone(true);
         transaction.complete();
      }

      private void migrateMessage(Stack<Transaction> stack, Transaction source, Transaction target, int level) {
         Transaction current = level < stack.size() ? stack.get(level) : null;
         boolean shouldKeep = false;

         for (Message child : source.getChildren()) {
            if (child != current) {
               target.addChild(child);
            } else {
               DefaultTransaction cloned = new DefaultTransaction(current.getType(), current.getName(),
                     DefaultMessageTreeManager.this);

               cloned.setTimestamp(current.getTimestamp());
               cloned.setDurationInMicros(current.getDurationInMicros());
               cloned.addData(current.getData().toString());
               cloned.setStatus(Message.SUCCESS);

               target.addChild(cloned);
               migrateMessage(stack, current, cloned, level + 1);
               shouldKeep = true;
            }
         }

         source.getChildren().clear();

         if (shouldKeep) { // add it back
            source.addChild(current);
         }
      }

      public void truncateAndFlush(Context ctx, long timestamp) {
         MessageTree tree = ctx.m_tree;
         Stack<Transaction> stack = ctx.m_stack;
         Message message = tree.getMessage();

         if (message instanceof DefaultTransaction) {
            String id = tree.getMessageId();

            if (id == null) {
               id = m_factory.getNextId();
               tree.setMessageId(id);
            }

            String rootId = tree.getRootMessageId();
            String childId = m_factory.getNextId();
            DefaultTransaction source = (DefaultTransaction) message;
            DefaultTransaction target = new DefaultTransaction(source.getType(), source.getName(),
                  DefaultMessageTreeManager.this);

            target.setTimestamp(source.getTimestamp());
            target.setDurationInMicros(source.getDurationInMicros());
            target.addData(source.getData().toString());
            target.setStatus(Message.SUCCESS);

            migrateMessage(stack, source, target, 1);

            for (int i = stack.size() - 1; i >= 0; i--) {
               DefaultTransaction t = (DefaultTransaction) stack.get(i);

               t.setTimestamp(timestamp);
               t.setDurationStart(System.nanoTime());
            }

            DefaultEvent next = new DefaultEvent("RemoteCall", "Next");

            next.addData(childId);
            next.setStatus(Message.SUCCESS);
            target.addChild(next);

            // tree is the parent, and m_tree is the child.
            MessageTree t = tree.copy();

            t.setMessage(target);

            ctx.m_tree.setMessageId(childId);
            ctx.m_tree.setParentMessageId(id);
            ctx.m_tree.setRootMessageId(rootId != null ? rootId : id);

            ctx.m_length = stack.size();
            ctx.m_totalDurationInMicros = ctx.m_totalDurationInMicros + target.getDurationInMicros();

            flush(t);
         }
      }

      public void validate(Transaction parent, Transaction transaction) {
         if (transaction.isStandalone()) {
            List<Message> children = transaction.getChildren();
            int len = children.size();

            for (int i = 0; i < len; i++) {
               Message message = children.get(i);

               if (message instanceof Transaction) {
                  validate(transaction, (Transaction) message);
               }
            }

            if (!transaction.isCompleted() && transaction instanceof DefaultTransaction) {
               // missing transaction end, log a BadInstrument event so that
               // developer can fix the code
               markAsNotCompleted((DefaultTransaction) transaction);
            }
         } else if (!transaction.isCompleted()) {
            if (transaction instanceof DefaultForkedTransaction) {
               // link it as run away message since the forked transaction is not completed yet
               linkAsRunAway((DefaultForkedTransaction) transaction);
            } else if (transaction instanceof DefaultTaggedTransaction) {
               // link it as run away message since the forked transaction is not completed yet
               markAsRunAway(parent, (DefaultTaggedTransaction) transaction);
            }
         }
      }
   }
}

package com.dianping.cat.message.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.Cat;
import org.unidal.cat.CatConstant;
import org.unidal.cat.config.ClientConfigurationManager;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.analyzer.EventAggregator;
import com.dianping.cat.analyzer.TransactionAggregator;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.NativeMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.status.AbstractCollector;
import com.dianping.cat.status.StatusExtensionRegister;

@Named(type = MessageSender.class)
public class TcpSocketSender extends ContainerHolder implements Task, MessageSender, LogEnabled {
   private static final int SIZE = 5000;

   @Inject(NativeMessageCodec.ID)
   private MessageCodec m_codec;

   @Inject
   private MessageStatistics m_statistics;

   @Inject
   private ClientConfigurationManager m_configManager;

   @Inject
   private MessageIdFactory m_factory;

   private MessageQueue m_queue = new DefaultMessageQueue(SIZE);

   private MessageQueue m_atomicQueue = new DefaultMessageQueue(SIZE);

   private ChannelManager m_channelManager;

   private Logger m_logger;

   private boolean m_active;

   private AtomicInteger m_errors = new AtomicInteger();

   private AtomicInteger m_sampleCount = new AtomicInteger();

   private CountDownLatch m_catServerLatch;

   private static final int MAX_CHILD_NUMBER = 200;

   private static final int MAX_DURATION = 1000 * 30;

   private static final long HOUR = 1000 * 60 * 60L;

   @Override
   public void contextualize(Context context) throws ContextException {
      super.contextualize(context);

      try {
         m_catServerLatch = (CountDownLatch) context.get("cat.server.latch");
      } catch (Exception e) {
         // ignore it
      }
   }

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   @Override
   public String getName() {
      return getClass().getSimpleName();
   }

   private boolean hitSample(double sampleRatio) {
      int count = m_sampleCount.incrementAndGet();

      return count % ((int) (1.0 / sampleRatio)) == 0;
   }

   @Override
   public void initialize(List<InetSocketAddress> addresses) {
      if (Cat.isEnabled()) {
         m_channelManager = new ChannelManager(m_logger, addresses, m_configManager, m_factory, m_catServerLatch);

         Threads.forGroup(CatConstant.CAT).start(this);
         Threads.forGroup(CatConstant.CAT).start(m_channelManager);

         Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
               m_logger.info("shut down cat client in runtime shut down hook!");
               shutdown();
            }
         });

         StatusExtensionRegister.getInstance().register(new AbstractCollector() {
            @Override
            public String getId() {
               return "cat.status";
            }

            @Override
            public Map<String, String> getProperties() {
               Map<String, String> map = new LinkedHashMap<String, String>();

               map.put("cat.status.send.queue.size", String.valueOf(m_queue.size()));
               map.put("cat.status.atomic.queue.size", String.valueOf(m_atomicQueue.size()));

               Map<String, Long> values = m_statistics.getStatistics();

               for (Entry<String, Long> entry : values.entrySet()) {
                  map.put(entry.getKey(), String.valueOf(entry.getValue()));
               }

               return map;
            }
         });
      }
   }

   private boolean isSameHour(long time1, long time2) {
      int hour1 = (int) (time1 / HOUR);
      int hour2 = (int) (time2 / HOUR);

      return hour1 == hour2;
   }

   private void localProcessTransaction(Transaction transaction) {
      TransactionAggregator.logTransaction(transaction);
      List<Message> child = transaction.getChildren();

      for (Message message : child) {
         if (message instanceof Transaction) {
            localProcessTransaction((Transaction) message);
         } else if (message instanceof Event) {
            EventAggregator.logEvent((Event) message);
         }
      }
   }

   private void localProcessTree(MessageTree tree) {
      Message message = tree.getMessage();

      if (message instanceof Transaction) {
         localProcessTransaction((Transaction) message);
      } else if (message instanceof Event) {
         EventAggregator.logEvent((Event) message);
      }
   }

   private void logQueueFullInfo(MessageTree tree) {
      if (m_statistics != null) {
         m_statistics.onOverflowed(tree);
      }

      int count = m_errors.incrementAndGet();

      if (count % 1000 == 0 || count == 1) {
         m_logger.error("Message queue is full in tcp socket sender! Count: " + count);
      }

      tree = null;
   }

   private MessageTree mergeTree(MessageQueue queue) {
      int max = MAX_CHILD_NUMBER;
      DefaultTransaction tran = new DefaultTransaction("_CatMergeTree", "_CatMergeTree", null);
      MessageTree first = queue.poll();
      final Message message = first.getMessage();
      final long timestamp = message.getTimestamp();

      tran.setStatus(Transaction.SUCCESS);
      tran.setCompleted(true);
      tran.setDurationStart(timestamp);
      tran.setTimestamp(timestamp);
      tran.setDurationInMicros(0);
      tran.addChild(message);

      while (max >= 0) {
         MessageTree tree = queue.peek();

         if (tree != null) {
            long nextTimestamp = tree.getMessage().getTimestamp();

            if (isSameHour(timestamp, nextTimestamp)) {
               tree = queue.poll();

               if (tree == null) {
                  break;
               }
               tran.addChild(tree.getMessage());
               max--;
            } else {
               break;
            }
         } else {
            break;
         }
      }
      ((DefaultMessageTree) first).setMessage(tran);
      return first;
   }

   private void offer(MessageTree tree) {
      if (m_configManager.getConfig().isAtomic(tree)) {
         boolean result = m_atomicQueue.offer(tree);

         if (!result) {
            logQueueFullInfo(tree);
         }
      } else {
         boolean result = m_queue.offer(tree);

         if (!result) {
            logQueueFullInfo(tree);
         }
      }
   }

   private void processAtomicMessage() {
      while (true) {
         if (shouldMerge(m_atomicQueue)) {
            MessageTree tree = mergeTree(m_atomicQueue);
            boolean result = m_queue.offer(tree);

            if (!result) {
               logQueueFullInfo(tree);
            }
         } else {
            break;
         }
      }
   }

   private void processNormalMessage() {
      while (true) {
         ChannelFuture channel = m_channelManager.channel();

         if (channel != null) {
            try {
               MessageTree tree = m_queue.poll();

               if (tree != null) {
                  sendInternal(channel, tree);
                  tree.setMessage(null);
               } else {
                  try {
                     Thread.sleep(5);
                  } catch (Exception e) {
                     m_active = false;
                  }
                  break;
               }
            } catch (Throwable t) {
               m_logger.error("Error when sending message over TCP socket!", t);
            }
         } else {
            long current = System.currentTimeMillis();
            long oldTimestamp = current - HOUR;

            while (true) {
               try {
                  MessageTree tree = m_queue.peek();

                  if (tree != null && tree.getMessage().getTimestamp() < oldTimestamp) {
                     MessageTree discradTree = m_queue.poll();

                     if (discradTree != null) {
                        m_statistics.onOverflowed(discradTree);
                     }
                  } else {
                     break;
                  }
               } catch (Exception e) {
                  m_logger.error(e.getMessage(), e);
                  break;
               }
            }

            try {
               Thread.sleep(5);
            } catch (Exception e) {
               m_active = false;
            }
         }
      }
   }

   @Override
   public void run() {
      m_active = true;

      while (m_active && Cat.isEnabled()) {
         processAtomicMessage();
         processNormalMessage();
      }

      processAtomicMessage();

      while (true) {
         MessageTree tree = m_queue.poll();

         if (tree != null) {
            ChannelFuture channel = m_channelManager.channel();

            if (channel != null) {
               sendInternal(channel, tree);
            } else {
               offer(tree);
            }
         } else {
            break;
         }
      }
   }

   @Override
   public void send(MessageTree tree) {
      if (!m_configManager.getConfig().isBlocked()) {
         double sampleRatio = m_configManager.getConfig().getSampleRatio();

         if (tree.canDiscard() && sampleRatio < 1.0) {
            if (hitSample(sampleRatio)) { // trace log
               offer(tree);
            } else {
               localProcessTree(tree); // compute with 1s in local machine
            }
         } else {
            offer(tree);
         }
      }
   }

   private void sendInternal(ChannelFuture channel, MessageTree tree) {
      if (tree.getMessageId() == null) {
         tree.setMessageId(m_factory.getNextId());
      }

      ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(10 * 1024); // 10K

      buf.writeInt(0); // placeholder of length
      m_codec.encode(tree, buf);

      int size = buf.readableBytes();

      buf.setInt(0, size - 4); // length
      channel.channel().writeAndFlush(buf);

      if (m_statistics != null) {
         m_statistics.onBytes(size);
      }
   }

   public void sendMessageForTest(MessageTree tree) {
      if (tree.getMessageId() == null) {
         tree.setMessageId(m_factory.getNextId());
      }

      ChannelFuture future = m_channelManager.channel();

      if (future != null) {
         ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(10 * 1024); // 10K

         buf.writeInt(0); // placeholder of length
         m_codec.encode(tree, buf);

         int size = buf.readableBytes();

         buf.setInt(0, size - 4); // length
         future.channel().writeAndFlush(buf);

         if (m_statistics != null) {
            m_statistics.onBytes(size);
         }
      }
   }

   private boolean shouldMerge(MessageQueue queue) {
      MessageTree tree = queue.peek();

      if (tree != null) {
         long firstTime = tree.getMessage().getTimestamp();

         if (System.currentTimeMillis() - firstTime > MAX_DURATION || queue.size() >= MAX_CHILD_NUMBER) {
            return true;
         }
      }
      return false;
   }

   @Override
   public void shutdown() {
      m_active = false;
      m_channelManager.shutdown();
   }
}

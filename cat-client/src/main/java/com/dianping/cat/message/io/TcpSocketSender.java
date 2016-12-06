package com.dianping.cat.message.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.CatConstant;
import org.unidal.cat.config.ClientConfigurationManager;
import org.unidal.cat.config.ClientEnvironmentSettings;
import org.unidal.cat.message.codec.NativeMessageCodec;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@Named(type = MessageSender.class)
public class TcpSocketSender extends ContainerHolder implements Task, MessageSender, LogEnabled {
   public static final int SIZE = 5000;

   private static final int MAX_CHILD_NUMBER = 200;

   @Inject(NativeMessageCodec.ID)
   private MessageCodec m_codec;

   @Inject
   private MessageStatistics m_statistics;

   @Inject
   private ClientConfigurationManager m_configManager;

   @Inject
   private ClientEnvironmentSettings m_settings;

   private MessageQueue m_queue = new DefaultMessageQueue(SIZE);

   private MessageQueue m_atomicTrees = new DefaultMessageQueue(SIZE);

   private List<InetSocketAddress> m_serverAddresses;

   private ChannelManager m_manager;

   private Logger m_logger;

   private transient boolean m_active;

   private AtomicInteger m_errors = new AtomicInteger();

   private AtomicInteger m_attempts = new AtomicInteger();

   private CountDownLatch m_catServerLatch;

   private boolean checkWritable(ChannelFuture future) {
      boolean isWriteable = false;
      Channel channel = future.channel();

      if (future != null && channel.isOpen()) {
         if (channel.isActive() && channel.isWritable()) {
            isWriteable = true;
         } else {
            int count = m_attempts.incrementAndGet();

            if (count % 1000 == 0 || count == 1) {
               m_logger.error("Netty write buffer is full! Attempts: " + count);
            }
         }
      }

      return isWriteable;
   }

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   @Override
   public String getName() {
      return "TcpSocketSender";
   }

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
   public void initialize() {
      m_manager = new ChannelManager(m_logger, m_serverAddresses, m_queue, m_configManager, m_catServerLatch);

      Threads.forGroup(CatConstant.CAT).start(this);
      Threads.forGroup(CatConstant.CAT).start(m_manager);

      if (!m_settings.isTestMode()) {
         Threads.forGroup(CatConstant.CAT).start(new MergeAtomicTask());
      }
   }

   private boolean isAtomicMessage(MessageTree tree) {
      Message message = tree.getMessage();

      if (message instanceof Transaction) {
         String type = message.getType();

         if (type.startsWith("Cache.") || "SQL".equals(type)) {
            return true;
         } else {
            return false;
         }
      } else {
         return true;
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

   private MessageTree mergeTree(MessageQueue trees) {
      int max = MAX_CHILD_NUMBER;
      DefaultTransaction tran = new DefaultTransaction("_CatMergeTree", "_CatMergeTree", null);
      MessageTree first = trees.poll();

      tran.setStatus(Transaction.SUCCESS);
      tran.setCompleted(true);
      tran.addChild(first.getMessage());
      tran.setTimestamp(first.getMessage().getTimestamp());
      long lastTimestamp = 0;
      long lastDuration = 0;

      while (max >= 0) {
         MessageTree tree = trees.poll();

         if (tree == null) {
            tran.setDurationInMillis(lastTimestamp - tran.getTimestamp() + lastDuration);
            break;
         }
         lastTimestamp = tree.getMessage().getTimestamp();
         if (tree.getMessage() instanceof DefaultTransaction) {
            lastDuration = ((DefaultTransaction) tree.getMessage()).getDurationInMillis();
         } else {
            lastDuration = 0;
         }
         tran.addChild(tree.getMessage());
         max--;
      }
      ((DefaultMessageTree) first).setMessage(tran);
      return first;
   }

   @Override
   public void run() {
      m_active = true;

      while (m_active) {
         ChannelFuture channel = m_manager.channel();

         if (channel != null && checkWritable(channel)) {
            try {
               MessageTree tree = m_queue.poll();

               if (tree != null) {
                  sendInternal(tree);
                  tree.setMessage(null);
               }

            } catch (Throwable t) {
               m_logger.error("Error when sending message over TCP socket!", t);
            }
         } else {
            try {
               Thread.sleep(5);
            } catch (Exception e) {
               // ignore it
               m_active = false;
            }
         }
      }
   }

   @Override
   public void send(MessageTree tree) {
      if (isAtomicMessage(tree)) {
         boolean result = m_atomicTrees.offer(tree);

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

   private void sendInternal(MessageTree tree) {
      ChannelFuture future = m_manager.channel();
      ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(10 * 1024); // 10K

      buf.writeInt(0); // placeholder of length

      m_codec.encode(tree, buf);

      int size = buf.readableBytes();

      buf.setInt(0, size - 4); // length

      Channel channel = future.channel();

      channel.writeAndFlush(buf);

      if (m_statistics != null) {
         m_statistics.onBytes(size);
      }
   }

   public void setServerAddresses(List<InetSocketAddress> serverAddresses) {
      m_serverAddresses = serverAddresses;
   }

   private boolean shouldMerge(MessageQueue trees) {
      MessageTree tree = trees.peek();

      if (tree != null) {
         long firstTime = tree.getMessage().getTimestamp();
         int maxDuration = 1000 * 30;

         if (System.currentTimeMillis() - firstTime > maxDuration || trees.size() >= MAX_CHILD_NUMBER) {
            return true;
         }
      }
      return false;
   }

   @Override
   public void shutdown() {
      m_active = false;
      m_manager.shutdown();
   }

   public class MergeAtomicTask implements Task {
      @Override
      public String getName() {
         return getClass().getSimpleName();
      }

      @Override
      public void run() {
         while (true) {
            if (shouldMerge(m_atomicTrees)) {
               MessageTree tree = mergeTree(m_atomicTrees);
               boolean result = m_queue.offer(tree);

               if (!result) {
                  logQueueFullInfo(tree);
               }
            } else {
               try {
                  Thread.sleep(5);
               } catch (InterruptedException e) {
                  break;
               }
            }
         }
      }

      @Override
      public void shutdown() {
      }
   }

}

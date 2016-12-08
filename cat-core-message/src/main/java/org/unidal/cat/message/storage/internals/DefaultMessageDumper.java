package org.unidal.cat.message.storage.internals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.CatConstant;
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.MessageDumper;
import org.unidal.cat.message.storage.MessageProcessor;
import org.unidal.cat.message.storage.exception.MessageQueueFullException;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;

@Named(type = MessageDumper.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultMessageDumper extends ContainerHolder implements MessageDumper, LogEnabled {
   @Inject
   private BlockDumperManager m_blockDumperManager;

   @Inject("local")
   private BucketManager m_bucketManager;

   @Inject
   private ServerStatisticManager m_statisticManager;

   @Inject
   private ServerConfigManager m_configManager;

   private List<BlockingQueue<MessageTree>> m_queues = new ArrayList<BlockingQueue<MessageTree>>();

   private List<MessageProcessor> m_processors = new ArrayList<MessageProcessor>();

   private AtomicInteger m_failCount = new AtomicInteger(-1);

   private long m_total;

   private BalancePolicy m_policy;

   private Logger m_logger;

   @Override
   public void awaitTermination(int hour) throws InterruptedException {
      closeMessageProcessor();
      m_blockDumperManager.close(hour);
      m_bucketManager.closeBuckets(hour);
   }

   private void closeMessageProcessor() throws InterruptedException {
      while (true) {
         boolean allEmpty = true;

         for (BlockingQueue<MessageTree> queue : m_queues) {
            if (!queue.isEmpty()) {
               allEmpty = false;
               break;
            }
         }

         if (allEmpty) {
            break;
         } else {
            TimeUnit.MILLISECONDS.sleep(1);
         }
      }

      for (MessageProcessor processor : m_processors) {
         processor.shutdown();
         super.release(processor);
      }
   }

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   public void initialize(int hour) {
      DefaultBlock.COMMPRESS_TYPE = CompressType.getCompressTye(m_configManager.getStorageCompressType());
      DefaultBlock.DEFLATE_LEVEL = m_configManager.getStorageDeflateLevel();
      DefaultBlock.MAX_SIZE = m_configManager.getStorageMaxBlockSize();

      m_logger.info("set compress type :" + DefaultBlock.COMMPRESS_TYPE.toString());
      m_logger.info("set compress level:" + DefaultBlock.DEFLATE_LEVEL);
      m_logger.info("set default block size:" + DefaultBlock.MAX_SIZE);

      int processThreads = m_configManager.getMessageProcessorThreads();

      m_policy = new BalancePolicy(processThreads);

      for (int i = 0; i < processThreads; i++) {
         BlockingQueue<MessageTree> queue = new ArrayBlockingQueue<MessageTree>(10000);
         MessageProcessor processor = lookup(MessageProcessor.class);

         m_queues.add(queue);
         m_processors.add(processor);

         processor.initialize(hour, i, queue);
         Threads.forGroup(CatConstant.CAT).start(processor);
      }
   }

   @Override
   public void process(MessageTree tree) {
      MessageId id = tree.getFormatMessageId();
      String domain = id.getDomain();
      int index = m_policy.getIndex(id);

      BlockingQueue<MessageTree> queue = m_queues.get(index);
      boolean success = queue.offer(tree);

      if (!success) {
         m_statisticManager.addMessageDumpLoss(1);

         if ((m_failCount.incrementAndGet() % 100) == 0) {
            Cat.logError(new MessageQueueFullException("Error when adding message to queue, fails: " + m_failCount));

            m_logger.info("message tree queue is full " + m_failCount + " index " + index);
            // tree.getBuffer().release();
         }
      } else {
         m_statisticManager.addMessageSize(domain, tree.getBuffer().readableBytes());

         if ((++m_total) % CatConstants.SUCCESS_COUNT == 0) {
            m_statisticManager.addMessageDump(CatConstants.SUCCESS_COUNT);
         }
      }
   }

   static class BalancePolicy {
      private int m_count;

      public BalancePolicy(int count) {
         m_count = count;
      }

      public int getIndex(MessageId id) {
         String domain = id.getDomain();
         String ip = id.getIpAddressInHex();
         long time = System.currentTimeMillis();
         long index = domain.hashCode() + ip.hashCode() + time / 100;

         // hashed by domain+ ip + time slice
         return (int) Math.abs(index % m_count);
      }
   }
}

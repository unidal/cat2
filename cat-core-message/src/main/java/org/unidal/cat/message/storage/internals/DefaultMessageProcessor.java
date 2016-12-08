package org.unidal.cat.message.storage.internals;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.cat.message.storage.Block;
import org.unidal.cat.message.storage.BlockDumper;
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.cat.message.storage.MessageFinder;
import org.unidal.cat.message.storage.MessageFinderManager;
import org.unidal.cat.message.storage.MessageProcessor;
import org.unidal.helper.Dates;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageProcessor.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultMessageProcessor implements MessageProcessor, MessageFinder {
   @Inject
   private BlockDumperManager m_blockDumperManager;

   @Inject
   private MessageFinderManager m_finderManager;

   private BlockDumper m_dumper;

   private int m_index;

   private BlockingQueue<MessageTree> m_queue;

   private ConcurrentMap<String, Block> m_blocks = new ConcurrentHashMap<String, Block>(1024);

   private int m_hour;

   private AtomicBoolean m_enabled;

   private CountDownLatch m_latch;

   private int m_count;

   @Override
   public ByteBuf find(MessageId id) {
      String domain = id.getDomain();
      Block block = m_blocks.get(domain);

      if (block != null) {
         return block.find(id);
      }

      return null;
   }

   @Override
   public String getName() {
      return getClass().getSimpleName() + "-" + Dates.from(TimeUnit.HOURS.toMillis(m_hour)).hour() + "-" + m_index;
   }

   @Override
   public void initialize(int hour, int index, BlockingQueue<MessageTree> queue) {
      m_index = index;
      m_queue = queue;
      m_enabled = new AtomicBoolean(true);
      m_dumper = m_blockDumperManager.findOrCreate(hour);
      m_hour = hour;
      m_latch = new CountDownLatch(1);
      m_finderManager.register(hour, this);
   }

   private boolean isMonitor() {
      return (++m_count) % 1000 == 0;
   }

   private MessageTree pollMessage() throws InterruptedException {
      return m_queue.poll(5, TimeUnit.MILLISECONDS);
   }

   private void processMessage(MessageTree tree) {
      MessageId id = tree.getFormatMessageId();
      String domain = id.getDomain();
      int hour = id.getHour();
      Block block = m_blocks.get(domain);

      if (block == null) {
         Block b = new DefaultBlock(domain, hour);

         if ((block = m_blocks.putIfAbsent(domain, b)) == null) {
            block = b;
         }
      }

      try {
         if (block.isFull()) {
            // double check here
            synchronized (block) {
               block = m_blocks.get(domain);

               if (block.isFull()) {
                  block.finish();
                  m_dumper.dump(block);

                  // use a new block for following messages
                  block = new DefaultBlock(domain, hour);
                  m_blocks.put(domain, block);
               }
            }
         }

         block.pack(id, tree.getBuffer());
      } catch (Throwable e) {
         Cat.logError(e);
      }
   }

   @Override
   public void run() {
      MessageTree tree;

      try {
         while (m_enabled.get() || !m_queue.isEmpty()) {
            tree = pollMessage();

            if (tree != null) {
               if (isMonitor()) {
                  Transaction t = Cat.newTransaction("Processor", "index-" + m_index);

                  processMessage(tree);
                  t.setStatus(Transaction.SUCCESS);
                  t.complete();
               } else {
                  processMessage(tree);
               }
            }
         }
      } catch (InterruptedException e) {
         // ignore it
      }

      Cat.logEvent("BlockSize", String.valueOf(m_blocks.size()), Event.SUCCESS, m_blocks.keySet().toString());

      for (Block block : m_blocks.values()) {
         try {
            block.finish();

            m_dumper.dump(block);
         } catch (Throwable e) {
            Cat.logError(e);
         }
      }

      m_blocks.clear();
      m_latch.countDown();
   }

   @Override
   public void shutdown() {
      m_enabled.set(false);

      try {
         m_latch.await();
      } catch (InterruptedException e) {
         // ignore it
      }
   }
}

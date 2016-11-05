package org.unidal.cat.core.alert.message;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = AlertMessageSink.class)
public class DefaultAlertMessageSink implements AlertMessageSink, Task {
   @Inject
   private AlertRecipientManager m_recipientManager;

   @Inject
   private AlertSenderManager m_senderManager;

   private BlockingQueue<AlertMessage> m_queue = new ArrayBlockingQueue<AlertMessage>(1000);

   private AtomicBoolean m_enabled;

   private CountDownLatch m_latch;

   @Override
   public void add(AlertMessage message) {
      m_queue.offer(message);
   }

   @Override
   public String getName() {
      return getClass().getSimpleName();
   }

   @Override
   public void run() {
      m_enabled = new AtomicBoolean(true);
      m_latch = new CountDownLatch(1);

      try {
         while (m_enabled.get()) {
            AlertMessage message = m_queue.poll(5, TimeUnit.MILLISECONDS);

            if (message != null) {
               send(message);
            }
         }
      } catch (InterruptedException e) {
         // ignore it
      }

      m_latch.countDown();
   }

   private void send(AlertMessage message) {
      String type = message.getRule().getRuleSet().getTypeName();
      Map<String, List<AlertRecipient>> recipients = m_recipientManager.getRecipients(message);

      for (Map.Entry<String, List<AlertRecipient>> e : recipients.entrySet()) {
         String action = e.getKey();

         for (AlertRecipient recipient : e.getValue()) {
            AlertSender sender = m_senderManager.getSender(type, action);

            try {
               sender.send(message, recipient);
            } catch (RuntimeException ex) {
               Cat.logError(ex);
            }
         }
      }
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

package org.unidal.cat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.unidal.cat.internals.CatInitializer;
import org.unidal.cat.internals.NullMessageProducer;
import org.unidal.lookup.ContainerLoader;

import com.dianping.cat.message.MessageProducer;

/**
 * This is the main entry point of CAT API.
 * 
 * @author qmwu2000@gmail.com
 * @since 2.0
 */
public class Cat {
   public static final Cat CAT = new Cat();

   private Map<String, Object> m_properties = new HashMap<String, Object>();

   private AtomicBoolean m_enabled = new AtomicBoolean(true);

   private AtomicBoolean m_initialized = new AtomicBoolean();

   private AtomicReference<MessageProducer> m_producer = new AtomicReference<MessageProducer>();

   private Cat() {
   }

   public static void count(String metric) {
      CAT.getProducer().logMetric(metric, "C", "1");
   }

   public static void count(String metric, int quantity) {
      CAT.getProducer().logMetric(metric, "C", String.valueOf(quantity));
   }

   /**
    * Disable the CAT client, no message will be construct and send to CAT server.
    * <p>
    */
   public static void disable() {
      CAT.m_enabled.set(false);

      // Disable the producer if needed
      if (CAT.m_producer.get() != null) {
         CAT.m_producer.get().disable();
      }
   }

   public static void duration(String metric, long durationInMillis) {
      CAT.getProducer().logMetric(metric, "T", String.valueOf(durationInMillis));
   }

   public static String getMessageId() {
      return CAT.getProducer().getManager().getThreadLocalMessageTree().getMessageId();
   }

   public static boolean isEnabled() {
      MessageProducer producer = CAT.m_producer.get();

      return producer != null && producer.isEnabled();
   }

   public static void logError(String message, Throwable cause) {
      CAT.getProducer().logError(message, cause);
   }

   public static void logError(Throwable cause) {
      CAT.getProducer().logError(cause);
   }

   public static void logEvent(String type, String name) {
      CAT.getProducer().logEvent(type, name);
   }

   public static void logRemoteCallClient(Context ctx, String serverDomain) {
      // TODO
   }

   public static void logRemoteCallServer(Context ctx) {
      // TODO
   }

   public static void setProperty(String key, Object value) {
      CAT.m_properties.put(key, value);
   }

   /**
    * Get the message producer. It will initialize the CAT client for first time.
    * <p>
    * 
    * No exception should be thrown out no matter if the CAT client is initialized or not.
    * 
    * @return the message producer
    */
   public MessageProducer getProducer() {
      if (m_producer.get() == null) {
         if (m_enabled.get()) {
            if (!m_initialized.get()) {
               synchronized (this) {
                  if (!m_initialized.get()) {
                     try {
                        CatInitializer initializer = ContainerLoader.getDefaultContainer().lookup(CatInitializer.class);

                        m_producer.set(initializer.initialize(m_properties));
                     } catch (Exception e) {
                        m_producer.set(NullMessageProducer.INSTANCE);
                        System.err.println("[WARN] Failed to initialize CAT, CAT is DISABLED!");
                        e.printStackTrace();
                     }

                     m_initialized.set(true);
                  }
               }
            }
         } else {
            m_producer.set(NullMessageProducer.INSTANCE);
         }
      }

      return m_producer.get();
   }

   public static interface Context {
      public void addProperty(String key, String value);

      public String getProperty(String key);
   }
}

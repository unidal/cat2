package org.unidal.cat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.unidal.cat.config.ClientEnvironmentSettings;
import org.unidal.cat.config.internals.DefaultClientEnvironmentSettings;
import org.unidal.cat.internals.CatClientInitializer;
import org.unidal.cat.message.MessagePolicy;
import org.unidal.lookup.ContainerLoader;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.NullMessageProducer;

/**
 * This is the main entry point of CAT API.
 * 
 * @author qmwu2000@gmail.com
 * @since 2.0
 */
public class Cat {
   public static final Cat CAT2 = new Cat();

   private Map<String, Object> m_properties = new HashMap<String, Object>();

   private AtomicBoolean m_enabled = new AtomicBoolean(true);

   private AtomicBoolean m_initialized = new AtomicBoolean();

   private AtomicReference<MessageProducer> m_producer = new AtomicReference<MessageProducer>();

   private MessagePolicy m_policy;

   private Cat() {
   }

   public static void count(String metric) {
      CAT2.getProducer().logMetric(metric, "C", "1");
   }

   public static void count(String metric, int quantity) {
      CAT2.getProducer().logMetric(metric, "C", String.valueOf(quantity));
   }

   public static void duration(String metric, long durationInMillis) {
      CAT2.getProducer().logMetric(metric, "T", String.valueOf(durationInMillis));
   }

   public static boolean isEnabled() {
      return CAT2.m_enabled.get() && (CAT2.m_policy == null || CAT2.m_policy != null && CAT2.m_policy.isEnabled());
   }

   public boolean isInitialized() {
      return m_initialized.get();
   }

   public static void logError(String message, Throwable cause) {
      CAT2.getProducer().logError(message, cause);
   }

   public static void logError(Throwable cause) {
      CAT2.getProducer().logError(cause);
   }

   public static void logEvent(String type, String name) {
      CAT2.getProducer().logEvent(type, name);
   }

   public static void logRemoteCallClient(Context ctx, String serverDomain) {
      // TODO
   }

   public static void logRemoteCallServer(Context ctx) {
      // TODO
   }

   public static Event newEvent(String type, String name) {
      return CAT2.getProducer().newEvent(type, name);
   }

   public static Transaction newTransaction(String type, String name) {
      return CAT2.getProducer().newTransaction(type, name);
   }

   public static Transaction newTransaction(Transaction parent, String type, String name) {
      return CAT2.getProducer().newTransaction(parent, type, name);
   }

   /**
    * Disable the CAT client, no message will be construct and send to CAT server.
    * <p>
    */
   public void disable() {
      m_enabled.set(false);

      // Disable message if needed
      if (m_policy != null) {
         m_policy.disable();
      }
   }

   public String getCatHome() {
      ClientEnvironmentSettings settings;

      try {
         settings = ContainerLoader.getDefaultContainer().lookup(ClientEnvironmentSettings.class);
      } catch (ComponentLookupException e) {
         settings = new DefaultClientEnvironmentSettings();
      }

      return settings.getCatHome();
   }

   public String getMessageId() {
      return CAT2.getProducer().getMessageId();
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
      initialize();

      return m_producer.get();
   }

   private synchronized void initialize() {
      if (m_producer.get() == null) {
         if (m_enabled.get()) {
            if (!m_initialized.get()) {
               synchronized (this) {
                  if (!m_initialized.get()) {
                     try {
                        PlexusContainer container = ContainerLoader.getDefaultContainer();
                        CatClientInitializer initializer = container.lookup(CatClientInitializer.class);
                        MessageProducer producer = initializer.initialize(m_properties);

                        m_producer.set(producer);
                        m_policy = container.lookup(MessagePolicy.class);
                     } catch (Exception e) {
                        System.err.println("[WARN] Failed to initialize CAT, CAT is DISABLED!");
                        e.printStackTrace();
                     }

                     m_initialized.set(true);
                  }
               }
            }
         } else {
         }
      }

      if (m_producer.get() == null) {
         m_producer.set(NullMessageProducer.NULL_MESSAGE_PRODUCER);
      }
   }

   public void setProperty(String key, Object value) {
      m_properties.put(key, value);
   }

   public static interface Context {
      public void addProperty(String key, String value);

      public String getProperty(String key);
   }
}

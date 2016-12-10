package com.dianping.cat;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.unidal.helper.Files;
import org.unidal.helper.Properties;
import org.unidal.initialization.DefaultModuleContext;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;
import org.unidal.initialization.ModuleInitializer;
import org.unidal.lookup.ContainerLoader;

import com.dianping.cat.analyzer.MetricAggregator;
import com.dianping.cat.configuration.EnvironmentHelper;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultSaxParser;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.TaggedTransaction;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.NullMessage;
import com.dianping.cat.message.internal.NullMessageManager;
import com.dianping.cat.message.internal.NullMessageProducer;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;

/**
 * This is the main entry point to the system.
 */
public class Cat {

   private MessageProducer m_producer;

   private MessageManager m_manager;

   private static int m_errorCount;

   private static Cat s_instance = new Cat();

   private static volatile boolean s_init = false;

   private static volatile boolean s_enabled = true;

   public final static String CLENT_CONFIG = "cat-client-config";

   public final static String UNKNOWN = "unknown";

   private final static String ROOT = "_catRootMessageId";

   private final static String PARENT = "_catParentMessageId";

   private final static String CHILD = "_catChildMessageId";

   private final static String DISCARD = "_catDiscard";

   private static void checkAndInitialize() {
      if (isEnabled()) {
         try {
            if (!s_init) {
               initialize();
            }
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * @deprecated
    * 
    * @return create next cat message id
    */
   public static String createMessageId() {
      if (isEnabled()) {
         try {
            return Cat.getProducer().createMessageId();
         } catch (Exception e) {
            errorHandler(e);
            return NullMessageProducer.NULL_MESSAGE_PRODUCER.createMessageId();
         }
      } else {
         return NullMessageProducer.NULL_MESSAGE_PRODUCER.createMessageId();
      }
   }

   @Deprecated
   public static void destroy() {
   }

   public static void disable() {
      s_enabled = false;
   }

   private static void errorHandler(Exception e) {
      if (isEnabled()) {
         if (m_errorCount++ % 100 == 0 || m_errorCount <= 3) {
            e.printStackTrace();
         }
      }
   }

   /**
    * @return current cat config path , default value is /data/appdatas/cat/
    */
   public static String getCatHome() {
      String catHome = Properties.forString().fromEnv().fromSystem().getProperty("CAT_HOME", "/data/appdatas/cat/");

      return catHome;
   }

   /**
    * @return current cat message tree id
    */
   public static String getCurrentMessageId() {
      if (isEnabled()) {
         try {
            MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

            if (tree != null) {
               String messageId = tree.getMessageId();

               if (messageId == null) {
                  messageId = Cat.getProducer().createMessageId();
                  tree.setMessageId(messageId);
               }
               return messageId;
            } else {
               return null;
            }
         } catch (Exception e) {
            errorHandler(e);
            return NullMessageProducer.NULL_MESSAGE_PRODUCER.createMessageId();
         }
      } else {
         return NullMessageProducer.NULL_MESSAGE_PRODUCER.createMessageId();
      }
   }

   /**
    * @return cat instance
    */
   public static Cat getInstance() {
      return s_instance;
   }

   /**
    * @return message manager
    */
   public static MessageManager getManager() {
      if (isEnabled()) {
         try {
            checkAndInitialize();
            MessageManager manager = s_instance.m_manager;

            if (manager != null) {
               return manager;
            } else {
               return NullMessageManager.NULL_MESSAGE_MANAGER;
            }
         } catch (Exception e) {
            errorHandler(e);
            return NullMessageManager.NULL_MESSAGE_MANAGER;
         }
      } else {
         return NullMessageManager.NULL_MESSAGE_MANAGER;
      }
   }

   /**
    * @return message producer
    */
   public static MessageProducer getProducer() {
      if (isEnabled()) {
         try {
            checkAndInitialize();

            MessageProducer producer = s_instance.m_producer;

            if (producer != null) {
               return producer;
            } else {
               return NullMessageProducer.NULL_MESSAGE_PRODUCER;
            }
         } catch (Exception e) {
            errorHandler(e);
            return NullMessageProducer.NULL_MESSAGE_PRODUCER;
         }
      } else {
         return NullMessageProducer.NULL_MESSAGE_PRODUCER;
      }
   }

   /**
    * this should be called during application initialization time
    */
   private static void initialize() {
      resetFromEnviorment();

      if (isEnabled()) {
         try {
            if (!s_init) {
               synchronized (s_instance) {
                  if (!s_init) {
                     // init CAT
                     org.unidal.cat.Cat.CAT.getProducer();

                     PlexusContainer container = ContainerLoader.getDefaultContainer();

                     s_instance.setContainer(container);
                     s_init = true;
                  }
               }
            }
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * this should be called during application initialization time
    * 
    * public is for test case
    */
   public static void initialize(ClientConfig config) {
      resetFromEnviorment();

      if (isEnabled()) {
         log("info", "init cat with config:" + config.toString());
         try {
            if (!s_init) {
               synchronized (s_instance) {
                  if (!s_init) {
                     System.setProperty(Cat.CLENT_CONFIG, config.toString());

                     PlexusContainer container = ContainerLoader.getDefaultContainer();
                     ModuleContext ctx = new DefaultModuleContext(container);
                     Module module = ctx.lookup(Module.class, CatClientModule.ID);

                     if (!module.isInitialized()) {
                        ModuleInitializer initializer = ctx.lookup(ModuleInitializer.class);

                        initializer.execute(ctx, module);
                     }
                     log("INFO", "Cat is lazy initialized!");
                     s_init = true;
                  }
               }
            }
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   @Deprecated
   public static void initialize(File configFile) {
   }

   /**
    * this should be called during application initialization time
    */
   public static void initialize(PlexusContainer container, File configFile) {
      if (true)
         return;

      resetFromEnviorment();

      if (isEnabled()) {
         try {
            String config = null;
            try {
               config = Files.forIO().readFrom(configFile, "utf-8");
            } catch (Exception e) {
               config = EnvironmentHelper.fetchClientConfig();
            }

            ClientConfig clientConfig = DefaultSaxParser.parse(config);

            clientConfig.setDomain(EnvironmentHelper.loadAppNameByProperty(UNKNOWN));

            System.setProperty(CLENT_CONFIG, clientConfig.toString());

            ModuleContext ctx = new DefaultModuleContext(container);
            Module module = ctx.lookup(Module.class, CatClientModule.ID);

            if (!module.isInitialized()) {
               ModuleInitializer initializer = ctx.lookup(ModuleInitializer.class);

               initializer.execute(ctx, module);
            }
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * this should be called during application initialization time
    * 
    * @param servers
    *           cat server ip, with default tcp port 2280, default http port 8080
    */
   public static void initialize(String... servers) {
      if (isEnabled()) {
         try {
            ClientConfig config = new ClientConfig();

            for (String server : servers) {
               config.addServer(new Server(server));
            }
            final String domain = EnvironmentHelper.loadAppNameByProperty(UNKNOWN);
            config.setDomain(domain);

            initialize(config);
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * this should be called during application initialization time
    * 
    * @param domain
    */
   public static void initializeByDomain(String domain) {
      if (isEnabled()) {
         try {
            log("info", "starting fetch client xml");

            String xml = null;
            String path = Cat.getCatHome() + "client.xml";
            File configFile = new File(path);

            if (configFile.exists()) {
               xml = Files.forIO().readFrom(configFile, "utf-8");
            } else {
               log("info", "fetch client config with remote cat url");
               xml = EnvironmentHelper.fetchClientConfig();
            }

            log("info", "end fetch client xml:" + xml);

            ClientConfig config = DefaultSaxParser.parse(xml);

            config.setDomain(EnvironmentHelper.loadAppNameByProperty(domain));
            initialize(config);
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * this should be called during application initialization time
    * 
    * @param domain
    * @param port
    * @param httpPort
    * @param servers
    *           cat server ip list
    */
   public static void initializeByDomain(String domain, int port, int httpPort, String... servers) {
      if (isEnabled()) {
         try {
            ClientConfig config = new ClientConfig();

            config.setDomain(EnvironmentHelper.loadAppNameByProperty(domain));

            for (String server : servers) {
               Server serverObj = new Server(server);

               serverObj.setHttpPort(httpPort);
               serverObj.setPort(port);
               config.addServer(serverObj);
            }

            initialize(config);
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * this should be called during application initialization time
    * 
    * @param domain
    * @param servers
    *           default tcp port 2280 http port 8080 cat server ip list
    */
   public static void initializeByDomain(String domain, String... servers) {
      if (isEnabled()) {
         try {
            initializeByDomain(domain, 2280, 80, servers);
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   public static boolean isEnabled() {
      return s_enabled;
   }

   public static boolean isInitialized() {
      return s_init;
   }

   private static void log(String severity, String message) {
      MessageFormat format = new MessageFormat("[{0,date,MM-dd HH:mm:ss.sss}] [{1}] [{2}] {3}");

      System.out.println(format.format(new Object[] { new Date(), severity, "cat", message }));
   }

   /**
    * log error to cat
    * 
    * @param message
    * @param cause
    */
   public static void logError(String message, Throwable cause) {
      if (isEnabled()) {
         try {
            Cat.getProducer().logError(message, cause);
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * log error to cat
    * 
    * @param cause
    */
   public static void logError(Throwable cause) {
      if (isEnabled()) {
         try {
            Cat.getProducer().logError(cause);
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * Log an event in one shot with SUCCESS status.
    * 
    * @param type
    *           event type
    * @param name
    *           event name
    */
   public static void logEvent(String type, String name) {
      if (isEnabled()) {
         try {
            Cat.getProducer().logEvent(type, name);
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * Log an event in one shot.
    * 
    * @param type
    *           event type
    * @param name
    *           event name
    * @param status
    *           "0" means success, otherwise means error code
    * @param nameValuePairs
    *           name value pairs in the format of "a=1&b=2&..."
    */
   public static void logEvent(String type, String name, String status, String nameValuePairs) {
      if (isEnabled()) {
         try {
            Cat.getProducer().logEvent(type, name, status, nameValuePairs);
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * @deprecated
    * 
    * @param type
    *           heartbeat type
    * @param name
    *           heartbeat name
    * @param status
    *           "0" means success, otherwise means error code
    * @param nameValuePairs
    *           name value pairs in the format of "a=1&b=2&..."
    */
   public static void logHeartbeat(String type, String name, String status, String nameValuePairs) {
   }

   @Deprecated
   public static void logMetric(String name, Object... keyValues) {
   }

   /**
    * Increase the counter specified by <code>name</code> by one.
    * 
    * @param name
    *           the name of the metric default count value is 1
    */
   public static void logMetricForCount(String name) {
      if (isEnabled()) {
         try {
            MetricAggregator.addCountMetric(name, 1);
            // logMetricInternal(name, "C", "1");
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * Increase the counter specified by <code>name</code> by one.
    * 
    * @param name
    *           the name of the metric
    */
   public static void logMetricForCount(String name, int quantity) {
      if (isEnabled()) {
         try {
            MetricAggregator.addCountMetric(name, quantity);
            // logMetricInternal(name, "C", String.valueOf(quantity));
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * Increase the metric specified by <code>name</code> by <code>durationInMillis</code>.
    * 
    * @param name
    *           the name of the metric
    * @param durationInMillis
    *           duration in milli-second added to the metric
    */
   public static void logMetricForDuration(String name, long durationInMillis) {
      if (isEnabled()) {
         try {
            MetricAggregator.addTimerMetric(name, durationInMillis);
            // logMetricInternal(name, "T", String.valueOf(durationInMillis));
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * @deprecated
    * 
    *             reset the counter specified by <code>name</code> by the latest value.
    * 
    * @param name
    *           the name of metric
    * @param quantity
    *           the latest quantity of the metric will cover the old value
    */
   public static void logMetricForLatestValue(String name, int quantity) {
      if (isEnabled()) {
         try {
            MetricAggregator.logMetricForLatestValue(name, quantity);
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * @deprecated This is deprecated api，please use logMetricCount
    * 
    *             Increase the sum specified by <code>name</code> by <code>value</code> only for one item.
    * 
    * @param name
    *           the name of the metric
    * @param value
    *           the value added to the metric
    */
   public static void logMetricForSum(String name, double value) {
      if (isEnabled()) {
         try {
            MetricAggregator.addCountMetric(name, (int) value);
            // logMetricInternal(name, "S", String.format("%.2f", value));
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * @deprecated
    * 
    *             this is deprecated api，please use logMetricCount or logMetricForDuration
    * 
    *             Increase the metric specified by <code>name</code> by <code>sum</code> for multiple items.
    * 
    * @param name
    *           the name of the metric
    * @param sum
    *           the sum value added to the metric
    * @param quantity
    *           the quantity to be accumulated
    */
   public static void logMetricForSum(String name, double sum, int quantity) {
      if (isEnabled()) {
         try {
            MetricAggregator.makeSureMetricExist(name).getCount().addAndGet(quantity);
            MetricAggregator.makeSureMetricExist(name).getSum().addAndGet((long) sum);
            // logMetricInternal(name, "S,C", String.format("%s,%.2f", quantity, sum));
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * logRemoteCallClient is used in rpc client
    * 
    * @param ctx
    *           ctx is rpc context ,such as duboo context , please use rpc context implement Context
    * @param domain
    *           domain is default, if use default config, the performance of server storage is bad。
    * @deprecated
    */
   public static void logRemoteCallClient(Context ctx) {
      if (isEnabled()) {
         try {
            logRemoteCallClient(ctx, "default");
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * logRemoteCallClient is used in rpc client
    * 
    * @param ctx
    *           ctx is rpc context ,such as duboo context , please use rpc context implement Context
    * @param domain
    *           domain is project name of rpc server name if it is not used in rpc server, just used in asyc thread ,you can use
    *           local domain name
    */
   public static void logRemoteCallClient(Context ctx, String domain) {
      if (isEnabled()) {
         try {
            MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
            String messageId = tree.getMessageId();

            if (messageId == null) {
               messageId = Cat.getProducer().createMessageId();
               tree.setMessageId(messageId);
            }

            String childId = Cat.getProducer().createRpcServerId(domain);
            Cat.logEvent(CatConstants.TYPE_REMOTE_CALL, "", Event.SUCCESS, childId);

            String root = tree.getRootMessageId();

            if (root == null) {
               root = messageId;
            }

            ctx.addProperty(ROOT, root);
            ctx.addProperty(PARENT, messageId);
            ctx.addProperty(CHILD, childId);

            final boolean canDiscard = Cat.getManager().getThreadLocalMessageTree().canDiscard();

            if (!canDiscard) {
               ctx.addProperty(DISCARD, "false");
            }
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * used in rpc server，use clild id as server message tree id.
    * 
    * @param ctx
    *           ctx is rpc context ,such as duboo context , please use rpc context implement Context
    */
   public static void logRemoteCallServer(Context ctx) {
      if (isEnabled()) {
         try {
            MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
            String childId = ctx.getProperty(CHILD);
            String rootId = ctx.getProperty(ROOT);
            String parentId = ctx.getProperty(PARENT);

            if (parentId != null) {
               tree.setParentMessageId(parentId);
            }
            if (rootId != null) {
               tree.setRootMessageId(rootId);
            }
            if (childId != null) {
               tree.setMessageId(childId);
            }

            String discard = ctx.getProperty(DISCARD);

            if (discard != null && "false".equals(discard)) {
               Cat.getManager().getThreadLocalMessageTree().setDiscard(false);
            }
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * Log an trace in one shot with SUCCESS status.
    * 
    * @param type
    *           trace type
    * @param name
    *           trace name
    */
   public static void logTrace(String type, String name) {
      if (isEnabled()) {
         try {
            Cat.getProducer().logTrace(type, name);
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * Log an trace in one shot.
    * 
    * @param type
    *           trace type
    * @param name
    *           trace name
    * @param status
    *           "0" means success, otherwise means error code
    * @param nameValuePairs
    *           name value pairs in the format of "a=1&b=2&..."
    */
   public static void logTrace(String type, String name, String status, String nameValuePairs) {
      if (isEnabled()) {
         try {
            Cat.getProducer().logTrace(type, name, status, nameValuePairs);
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * Create a new transaction with given type and name and duration, duration time in millisecond The transaction is auto completed
    * with default SUCCESS status
    * 
    * @param type
    *           transaction type
    * @param name
    *           transaction name
    */
   public static void newCompletedTransactionWithDuration(String type, String name, long duration) {
      if (isEnabled()) {
         try {
            final Transaction transaction = Cat.getProducer().newTransaction(type, name);

            try {
               transaction.setDurationInMillis(duration);

               transaction.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
               transaction.setStatus(e);
            } finally {
               transaction.complete();
            }
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   /**
    * Create a new event with given type and name.
    * 
    * @param type
    *           event type
    * @param name
    *           event name
    */
   public static Event newEvent(String type, String name) {
      if (isEnabled()) {
         try {
            return Cat.getProducer().newEvent(type, name);
         } catch (Exception e) {
            errorHandler(e);
            return NullMessage.EVENT;
         }
      } else {
         return NullMessage.EVENT;
      }
   }

   /**
    * @deprecated Create a forked transaction for child thread.
    * 
    * @param type
    *           transaction type
    * @param name
    *           transaction name
    * @return forked transaction
    */
   public static ForkedTransaction newForkedTransaction(String type, String name) {
      if (isEnabled()) {
         try {
            return Cat.getProducer().newForkedTransaction(type, name);
         } catch (Exception e) {
            errorHandler(e);
            return NullMessage.TRANSACTION;
         }
      } else {
         return NullMessage.TRANSACTION;
      }
   }

   /**
    * @deprecated
    * 
    *             Create a new heartbeat with given type and name.
    * 
    * @param type
    *           heartbeat type
    * @param name
    *           heartbeat name
    */
   public static Heartbeat newHeartbeat(String type, String name) {
      if (isEnabled()) {
         try {
            return Cat.getProducer().newHeartbeat(type, name);
         } catch (Exception e) {
            errorHandler(e);
            return NullMessage.HEARTBEAT;
         }
      } else {
         return NullMessage.HEARTBEAT;
      }
   }

   /**
    * @deprecated Create a tagged transaction for another process or thread.
    * 
    * @param type
    *           transaction type
    * @param name
    *           transaction name
    * @param tag
    *           tag applied to the transaction
    * @return tagged transaction
    */
   public static TaggedTransaction newTaggedTransaction(String type, String name, String tag) {
      return NullMessage.TRANSACTION;
   }

   /**
    * Create a new trace with given type and name.
    * 
    * @param type
    *           trace type
    * @param name
    *           trace name
    */
   public static Trace newTrace(String type, String name) {
      if (isEnabled()) {
         try {
            return Cat.getProducer().newTrace(type, name);
         } catch (Exception e) {
            errorHandler(e);
            return NullMessage.TRACE;
         }
      } else {
         return NullMessage.TRACE;
      }
   }

   /**
    * Create a new transaction with given type and name.
    * 
    * @param type
    *           transaction type
    * @param name
    *           transaction name
    */
   public static Transaction newTransaction(String type, String name) {
      if (isEnabled()) {
         try {
            return Cat.getProducer().newTransaction(type, name);
         } catch (Exception e) {
            errorHandler(e);
            return NullMessage.TRANSACTION;
         }
      } else {
         return NullMessage.TRANSACTION;
      }
   }

   /**
    * Create a new transaction with given type and name and duration, duration time in microsecond
    * 
    * @param type
    *           transaction type
    * @param name
    *           transaction name
    */
   public static Transaction newTransactionWithDuration(String type, String name, long duration) {
      if (isEnabled()) {
         try {
            final Transaction transaction = Cat.getProducer().newTransaction(type, name);

            transaction.setDurationInMillis(duration);

            return transaction;
         } catch (Exception e) {
            errorHandler(e);
            return NullMessage.TRANSACTION;
         }
      } else {
         return NullMessage.TRANSACTION;
      }
   }

   /**
    * @deprecated this should be called when a thread ends to clean some thread local data
    */
   public static void reset() {
   }

   public static void resetFromEnviorment() {
      String enable = Properties.forString().fromEnv().fromSystem().getProperty("CAT_ENABLED", "true");

      if ("false".equals(enable)) {
         s_enabled = false;
      }
   }

   public static void setMetricSlowThreshold(String key, int slow) {
      if (isEnabled()) {
         MetricAggregator.setMetricSlowThreshold(key, slow);
      }
   }

   /**
    * @deprecated this should be called when a thread starts to create some thread local data
    */
   public static void setup(String sessionToken) {
      if (isEnabled()) {
         try {
            Cat.getManager().setup();
         } catch (Exception e) {
            errorHandler(e);
         }
      }
   }

   private Cat() {
   }

   public void setContainer(PlexusContainer container) {
      try {
         m_manager = container.lookup(MessageManager.class);
         m_producer = container.lookup(MessageProducer.class);
      } catch (ComponentLookupException e) {
         throw new RuntimeException("Unable to get instance of MessageManager, "
               + "please make sure the environment was setup correctly!", e);
      }
   }

   public static interface Context {
      public void addProperty(String key, String value);

      public String getProperty(String key);
   }

   public static void initialize(PlexusContainer container) {
      s_instance.setContainer(container);
   }
}

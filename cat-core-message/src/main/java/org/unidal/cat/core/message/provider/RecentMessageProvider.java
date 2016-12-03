package org.unidal.cat.core.message.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.CatConstant;
import org.unidal.cat.core.message.config.MessageConfiguration;
import org.unidal.cat.core.message.service.MessageCodecService;
import org.unidal.helper.Files;
import org.unidal.helper.Threads;
import org.unidal.helper.Urls;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageProducer;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageProvider.class, value = RecentMessageProvider.ID)
public class RecentMessageProvider implements MessageProvider, Initializable {
   public static final String ID = "recent";

   @Inject
   private MessageCodecService m_codec;

   @Inject
   private MessageConfiguration m_config;

   private ExecutorService m_pool;

   @Override
   public MessageTree getMessage(final MessageContext ctx) {
      final Transaction t = Cat.getProducer().newTransaction("LogTree", "Recent");

      try {
         Map<String, Boolean> servers = m_config.getServers();
         int len = servers.size();
         List<Callable<MessageTree>> callables = new ArrayList<Callable<MessageTree>>(servers.size());

         for (Map.Entry<String, Boolean> e : servers.entrySet()) {
            if (e.getValue().booleanValue()) {
               final String server = e.getKey();

               callables.add(new Callable<MessageTree>() {
                  @Override
                  public MessageTree call() throws Exception {
                     ctx.setParentTransaction(t);

                     try {
                        return getRemoteMessage(ctx, server);
                     } catch (Throwable e) {
                        return null;
                     }
                  }
               });
            }
         }

         List<MessageTree> messages = new ArrayList<MessageTree>(len);
         int timeout = m_config.getRemoteCallReadTimeoutInMillis();

         try {
            List<Future<MessageTree>> futures = m_pool.invokeAll(callables, timeout, TimeUnit.MILLISECONDS);

            for (Future<MessageTree> future : futures) {
               if (future.isDone()) {
                  try {
                     MessageTree message = future.get();

                     if (message != null) {
                        messages.add(message);
                     }
                  } catch (InterruptedException e) {
                     e.printStackTrace();
                     break;
                  } catch (Exception e) {
                     e.printStackTrace();
                  }
               } else {
                  System.out.println("Future is not completed on timeout. " + future);
               }
            }
         } catch (InterruptedException e) {
            e.printStackTrace();
         }

         if (messages.isEmpty()) {
            t.setStatus(Message.SUCCESS);
            return null;
         } else {
            t.setStatus(Message.SUCCESS);
            return messages.get(0);
         }
      } catch (RuntimeException e) {
         Cat.logError(e);
         t.setStatus(e);
         throw e;
      } catch (Error e) {
         Cat.logError(e);
         t.setStatus(e);
         throw e;
      } finally {
         t.complete();
      }
   }

   private MessageTree getRemoteMessage(MessageContext ctx, String server) throws IOException {
      DefaultMessageProducer cat = (DefaultMessageProducer) Cat.getProducer();
      Transaction t = cat.newTransaction(ctx.getParentTransaction(), "LogTree.Remote", server);

      try {
         String url = ctx.buildURL(m_config.getServerUriPrefix(server));
         int ct = m_config.getRemoteCallConnectTimeoutInMillis();
         int rt = m_config.getRemoteCallReadTimeoutInMillis();

         t.addData(url);

         Map<String, List<String>> headers = new HashMap<String, List<String>>(2);
         InputStream in = Urls.forIO().connectTimeout(ct).readTimeout(rt)//
               .header("Accept-Encoding", "gzip").openStream(url, headers);

         if ("[gzip]".equals(String.valueOf(headers.get("Content-Encoding")))) {
            in = new GZIPInputStream(in);
         }

         t.setStatus(Message.SUCCESS);

         byte[] data = Files.forIO().readFrom(in);

         return m_codec.decodeNative(data);
      } catch (IOException e) {
         t.setStatus(e);
         throw e;
      } catch (RuntimeException e) {
         t.setStatus(e);
         throw e;
      } catch (Error e) {
         t.setStatus(e);
         throw e;
      } finally {
         t.complete();
      }
   }

   @Override
   public void initialize() throws InitializationException {
      int threads = m_config.getRemoteCallThreads();

      m_pool = Threads.forPool().getFixedThreadPool(CatConstant.CAT + "-Message", threads);
   }

   @Override
   public boolean isEligible(MessageContext ctx) {
      return ctx.isLocal();
   }
}

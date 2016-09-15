package org.unidal.cat.spi.message;

import io.netty.buffer.ByteBuf;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.spi.analysis.MessageDispatcher;
import org.unidal.cat.transport.decode.DecodeHandler;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.CatConstants;
import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;

@Named(type = DecodeHandler.class, value = PlainTextMessageCodec.ID)
public class PlainTextMessageDecodeHandler implements DecodeHandler, LogEnabled {
   @Inject(PlainTextMessageCodec.ID)
   private MessageCodec m_codec;

   @Inject
   private MessageConsumer m_consumer;

   @Inject
   private MessageDispatcher m_dispatcher;

   @Inject
   protected ServerConfigManager m_serverConfigManager;

   @Inject
   private ServerStatisticManager m_serverStateManager;

   private volatile long m_processCount;

   private Logger m_logger;

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   @Override
   public void handle(ByteBuf buf) {
      try {
         DefaultMessageTree tree = new DefaultMessageTree();

         buf.markReaderIndex();
         m_codec.decode(buf, tree);
         buf.resetReaderIndex();
         tree.setBuffer(buf); // buf with length at first

         // TODO remove m_consumer after all analyzers migrated
         m_consumer.consume(tree);
         m_dispatcher.dispatch(tree);

         m_processCount++;

         long flag = m_processCount % CatConstants.SUCCESS_COUNT;

         if (flag == 0) {
            m_serverStateManager.addMessageTotal(CatConstants.SUCCESS_COUNT);
         }
      } catch (Exception e) {
         m_serverStateManager.addMessageTotalLoss(1);
         m_logger.error(e.getMessage(), e);
      }
   }
}

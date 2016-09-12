package org.unidal.cat.core.message.page.home;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;

import org.unidal.cat.core.message.config.MessageConfiguration;
import org.unidal.cat.core.message.page.MessagePage;
import org.unidal.cat.core.message.service.MessageService;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;

public class Handler implements PageHandler<Context> {
   @Inject
   private JspViewer m_jspViewer;

   @Inject
   private MessageConfiguration m_config;

   @Inject
   private MessageService m_service;

   @Inject
   private MessageCodec m_htmlCodec;

   private MessageId getMessageId(String messageId) {
      try {
         if (messageId != null) {
            MessageId id = MessageId.parse(messageId);

            return id;
         }
      } catch (Exception e) {
         // ignore it
      }

      return null;
   }

   @Override
   @PayloadMeta(Payload.class)
   @InboundActionMeta(name = "home")
   public void handleInbound(Context ctx) throws ServletException, IOException {
      // display only, no action here
   }

   @Override
   @OutboundActionMeta(name = "home")
   public void handleOutbound(Context ctx) throws ServletException, IOException {
      Model model = new Model(ctx);
      Payload payload = ctx.getPayload();

      model.setAction(Action.VIEW);
      model.setPage(MessagePage.HOME);

      String msgId = payload.getMessageId();
      MessageId id = getMessageId(msgId);

      model.setMessageId(id);

      if (id != null) {
         MessageTree tree = m_service.getMessageTree(id);

         if (tree == null) {
            if (isArchived(id)) {
               Cat.logEvent("LogTree", "Archived:" + id.getDomain());
            } else {
               Cat.logEvent("LogTree", "Failure:" + id.getDomain());
            }
         } else {
            model.setMessageTree(tree);

            Cat.logEvent("LogTree", "Success");
         }
      }

      if (!ctx.isProcessStopped()) {
         m_jspViewer.view(ctx, model);
      }
   }

   private boolean isArchived(MessageId id) {
      Date startTime = ReportPeriod.DAY.getStartTime(new Date(id.getTimestamp()));
      int maxDays = m_config.getHdfsMaxStorageTime();

      return startTime.getTime() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(maxDays);
   }
}

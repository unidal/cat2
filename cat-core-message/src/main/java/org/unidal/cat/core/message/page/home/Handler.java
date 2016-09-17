package org.unidal.cat.core.message.page.home;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.unidal.cat.core.message.config.MessageConfiguration;
import org.unidal.cat.core.message.page.MessagePage;
import org.unidal.cat.core.message.provider.DefaultMessageContext;
import org.unidal.cat.core.message.provider.MessageContext;
import org.unidal.cat.core.message.provider.MessageProvider;
import org.unidal.cat.core.message.service.MessageCodecService;
import org.unidal.cat.core.message.service.MessageService;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;

public class Handler implements PageHandler<Context> {
   @Inject
   private JspViewer m_jspViewer;

   @Inject
   private MessageConfiguration m_config;

   @Inject
   private MessageProvider m_provider;

   @Inject
   private MessageService m_service;

   @Inject
   private MessageCodecService m_codec;

   private void handleAggregatedMessage(Context ctx, Model model, Payload payload) throws IOException {
      MessageId msgId = payload.getId();

      model.setMessageId(msgId);

      if (msgId != null) {
         MessageContext context = new DefaultMessageContext(msgId, m_config.isUseHdfs()) //
               .setProperty("op", "native");
         MessageTree tree = m_provider.getMessage(context);

         if (tree != null) {
            ByteBuf buf = null;

            if (payload.isWaterfall()) {
               buf = m_codec.encodeWaterfall(tree);
            } else {
               buf = m_codec.encodeHtml(tree);
            }

            buf.readInt(); // get rid of length
            model.setHtml(buf.toString(Charset.forName("utf-8")));
            model.setMessageTree(tree);
         }
      }
   }

   @Override
   @PayloadMeta(Payload.class)
   @InboundActionMeta(name = "home")
   public void handleInbound(Context ctx) throws ServletException, IOException {
      // display only, no action here
   }

   private void handleLocalMessage(Context ctx, Model model, Payload payload) throws IOException {
      MessageId msgId = payload.getId();
      Action action = payload.getAction();
      String id = payload.getMessageId();
      HttpServletResponse res = ctx.getHttpServletResponse();

      model.setMessageId(msgId);

      if (msgId != null) {
         MessageTree tree = m_service.getMessageTree(msgId);

         if (tree == null) {
            if (isArchived(msgId)) {
               Cat.logEvent("LogTree.State", "Archived:" + msgId.getDomain());
            } else {
               Cat.logEvent("LogTree.State", "Failure:" + msgId.getDomain());
            }

            res.sendError(SC_NOT_FOUND, String.format("Message(%s) is not found!", id));
         } else {
            Cat.logEvent("LogTree.State", "Success");

            ByteBuf buf;

            if (action == Action.NATIVE) {
               buf = m_codec.encodeNative(tree);
            } else {
               buf = m_codec.encodeHtml(tree);
            }

            sendResponse(ctx.getHttpServletRequest(), res, buf);
         }
      } else {
         Cat.logEvent("LogTree.State", "BadMessageId");
         res.sendError(SC_BAD_REQUEST, String.format("Invalid message id(%s)!", id));
      }

      ctx.stopProcess();
   }

   @Override
   @OutboundActionMeta(name = "home")
   public void handleOutbound(Context ctx) throws ServletException, IOException {
      Model model = new Model(ctx);
      Payload payload = ctx.getPayload();
      Action action = payload.getAction();

      model.setAction(Action.DEFAULT);
      model.setPage(MessagePage.HOME);

      switch (action) {
      case DEFAULT:
         handleAggregatedMessage(ctx, model, payload);
         break;
      case NATIVE:
      case HTML:
         handleLocalMessage(ctx, model, payload);
         break;
      default:
         break;
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

   private void sendResponse(HttpServletRequest req, HttpServletResponse res, ByteBuf buf) throws IOException {
      String acceptEncoding = req.getHeader("Accept-Encoding");
      int length = buf.readableBytes();

      if (acceptEncoding != null && acceptEncoding.toLowerCase().contains("gzip")) {
         GZIPOutputStream out = new GZIPOutputStream(res.getOutputStream());

         res.setContentType("text/html; charset=utf-8");
         res.setHeader("Content-Encoding", "gzip");
         buf.readBytes(out, length);
         out.finish();
         out.flush();
      } else {
         OutputStream out = res.getOutputStream();

         res.setContentLength(length);
         res.setContentType("application/octet-stream");
         buf.readBytes(out, length);
         out.flush();
      }
   }
}

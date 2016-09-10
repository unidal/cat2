package org.unidal.cat.plugin.transaction.config.page;

import java.io.IOException;

import javax.servlet.ServletException;

import org.unidal.cat.plugin.transaction.TransactionConfigService;
import org.unidal.cat.plugin.transaction.config.ConfigPage;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
   @Inject
   private JspViewer m_jspViewer;

   @Inject
   private TransactionConfigService m_configService;

   @Override
   @PayloadMeta(Payload.class)
   @InboundActionMeta(name = "transaction")
   public void handleInbound(Context ctx) throws ServletException, IOException {
      Payload payload = ctx.getPayload();
      Action action = payload.getAction();

      if (action == Action.EDIT) {
         if (payload.isUpdate() && !ctx.hasErrors()) {
            String content = payload.getContent();

            try {
               m_configService.setConfig(content);
            } catch (Exception e) {
               ctx.addError("config.update.error", e);
            }
         }
      }
   }

   @Override
   @OutboundActionMeta(name = "transaction")
   public void handleOutbound(Context ctx) throws ServletException, IOException {
      Model model = new Model(ctx);
      Payload payload = ctx.getPayload();
      Action action = payload.getAction();

      model.setAction(action);
      model.setPage(ConfigPage.TRANSACTION);
      model.setContent(m_configService.getConfig());
      
      if (!ctx.isProcessStopped()) {
         m_jspViewer.view(ctx, model);
      }
   }
}

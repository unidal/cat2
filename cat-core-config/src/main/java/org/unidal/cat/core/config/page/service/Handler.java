package org.unidal.cat.core.config.page.service;

import java.io.IOException;

import javax.servlet.ServletException;

import org.unidal.cat.core.config.page.ConfigPage;
import org.unidal.cat.core.config.spi.ConfigException;
import org.unidal.cat.core.config.spi.ConfigStoreManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
   @Inject
   private JspViewer m_jspViewer;

   @Inject
   private ConfigStoreManager m_manager;

   @Override
   @PayloadMeta(Payload.class)
   @InboundActionMeta(name = "service")
   public void handleInbound(Context ctx) throws ServletException, IOException {
      if (!ctx.hasErrors()) {
         Payload payload = ctx.getPayload();
         Action action = payload.getAction();

         switch (action) {
         case REFRESH:
            try {
               m_manager.reloadConfigStore(payload.getGroup(), payload.getName());
            } catch (ConfigException e) {
               ctx.addError("config.refresh.error", e);
            }

            break;
         default:
            break;
         }
      }
   }

   @Override
   @OutboundActionMeta(name = "service")
   public void handleOutbound(Context ctx) throws ServletException, IOException {
      Model model = new Model(ctx);

      model.setAction(Action.VIEW);
      model.setPage(ConfigPage.SERVICE);

      if (!ctx.isProcessStopped()) {
         m_jspViewer.view(ctx, model);
      }
   }
}

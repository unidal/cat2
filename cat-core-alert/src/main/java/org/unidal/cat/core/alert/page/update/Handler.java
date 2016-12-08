package org.unidal.cat.core.alert.page.update;

import static org.unidal.cat.core.alert.config.AlertConfigStoreGroup.ID;

import java.io.IOException;

import javax.servlet.ServletException;

import org.unidal.cat.core.alert.page.AlertPage;
import org.unidal.cat.core.config.spi.ConfigStore;
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
   private ConfigStoreManager m_storeManager;

   @Override
   @PayloadMeta(Payload.class)
   @InboundActionMeta(name = "update")
   public void handleInbound(Context ctx) throws ServletException, IOException {
      Payload payload = ctx.getPayload();
      Action action = payload.getAction();
      String name = payload.getName();

      if (action == Action.EDIT) {
         if (name != null && payload.isUpdate() && !ctx.hasErrors()) {
            String content = payload.getContent();

            try {
               m_storeManager.onChanged(ID, name, content);
            } catch (Exception e) {
               ctx.addError("config.update.error", e).addArgument("name", payload.getName());
            }
         }
      }
   }

   @Override
   @OutboundActionMeta(name = "update")
   public void handleOutbound(Context ctx) throws ServletException, IOException {
      Model model = new Model(ctx);
      Payload payload = ctx.getPayload();
      Action action = payload.getAction();
      String name = payload.getName();

      model.setAction(action);
      model.setPage(AlertPage.UPDATE);

      switch (action) {
      case EDIT:
         if (payload.isUpdate()) {
            model.setContent(payload.getContent());

            break;
         }
      default:
         ConfigStore store = m_storeManager.getConfigStore(ID, name);

         model.setContent(store.getConfig());
         break;
      }

      if (!ctx.isProcessStopped()) {
         m_jspViewer.view(ctx, model);
      }
   }
}

package org.unidal.cat.core.message.page.home;

import org.unidal.cat.core.message.page.MessagePage;
import org.unidal.cat.core.report.page.CoreReportPayload;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload extends CoreReportPayload<MessagePage, Action> {
   private MessagePage m_page;

   @FieldMeta("op")
   private Action m_action;

   public Payload() {
      super(MessagePage.HOME);
   }

   @Override
   public Action getAction() {
      return m_action;
   }

   @Override
   public MessagePage getPage() {
      return m_page;
   }

   public void setAction(String action) {
      m_action = Action.getByName(action, Action.VIEW);
   }

   @Override
   public void setPage(String page) {
      m_page = MessagePage.getByName(page, MessagePage.HOME);
   }

   @Override
   public void validate(ActionContext<?> ctx) {
      super.validate(ctx);
      
      if (m_action == null) {
         m_action = Action.VIEW;
      }
   }
}

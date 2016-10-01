package org.unidal.cat.core.alert.page.home;

import org.unidal.cat.core.alert.page.AlertPage;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<AlertPage, Action> {
   private AlertPage m_page;

   @FieldMeta("op")
   private Action m_action;

   public void setAction(String action) {
      m_action = Action.getByName(action, Action.VIEW);
   }

   @Override
   public Action getAction() {
      return m_action;
   }

   @Override
   public AlertPage getPage() {
      return m_page;
   }

   @Override
   public void setPage(String page) {
      m_page = AlertPage.getByName(page, AlertPage.HOME);
   }

   @Override
   public void validate(ActionContext<?> ctx) {
      if (m_action == null) {
         m_action = Action.VIEW;
      }
   }
}

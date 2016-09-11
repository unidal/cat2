package org.unidal.cat.core.config.page.service;

import org.unidal.cat.core.config.page.ConfigPage;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ConfigPage, Action> {
   private ConfigPage m_page;

   @FieldMeta("op")
   private Action m_action;

   @FieldMeta("group")
   private String m_group;

   @FieldMeta("name")
   private String m_name;

   @Override
   public Action getAction() {
      return m_action;
   }

   public String getGroup() {
      return m_group;
   }

   public String getName() {
      return m_name;
   }

   @Override
   public ConfigPage getPage() {
      return m_page;
   }

   public void setAction(String action) {
      m_action = Action.getByName(action, Action.VIEW);
   }

   @Override
   public void setPage(String page) {
      m_page = ConfigPage.getByName(page, ConfigPage.SERVICE);
   }

   @Override
   public void validate(ActionContext<?> ctx) {
      if (m_action == null) {
         m_action = Action.VIEW;
      }

      if (m_action == Action.REFRESH) {
         if (m_group == null) {
            ctx.addError("group.required");
         }

         if (m_name == null) {
            ctx.addError("name.required");
         }
      }
   }
}

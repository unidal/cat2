package org.unidal.cat.core.config.page.update;

import org.unidal.cat.core.config.page.ConfigPage;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.PathMeta;

public class Payload implements ActionPayload<ConfigPage, Action> {
   private ConfigPage m_page;

   @FieldMeta("op")
   private Action m_action;

   @FieldMeta("content")
   private String m_content;

   @FieldMeta("update")
   private boolean m_update;

   @PathMeta("path")
   private String[] m_path;

   @Override
   public Action getAction() {
      return m_action;
   }

   public String getContent() {
      return m_content;
   }

   @Override
   public ConfigPage getPage() {
      return m_page;
   }

   public String getReport() {
      if (m_path != null && m_path.length > 0) {
         return m_path[0];
      } else {
         return null;
      }
   }

   public boolean isUpdate() {
      return m_update;
   }

   public void setAction(String action) {
      m_action = Action.getByName(action, Action.VIEW);
   }

   @Override
   public void setPage(String page) {
      m_page = ConfigPage.getByName(page, ConfigPage.UPDATE);
   }

   @Override
   public void validate(ActionContext<?> ctx) {
      if (m_action == null) {
         m_action = Action.VIEW;
      }

      if (getReport() == null) {
         ctx.addError("report.invalid");
      }
   }
}

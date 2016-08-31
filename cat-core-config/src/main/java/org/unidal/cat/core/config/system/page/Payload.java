package org.unidal.cat.core.config.system.page;

import org.unidal.cat.core.config.system.SystemPage;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.PathMeta;

public class Payload implements ActionPayload<SystemPage, Action> {
   private SystemPage m_page;

   @FieldMeta("op")
   private Action m_action;

   @PathMeta("path")
   private String[] m_path;

   public void setAction(String action) {
      m_action = Action.getByName(action, Action.VIEW);
   }

   @Override
   public Action getAction() {
      return m_action;
   }

   @Override
   public SystemPage getPage() {
      return m_page;
   }

   @Override
   public void setPage(String page) {
      m_page = SystemPage.getByName(page, SystemPage.CONFIG);
   }

   /* used by config-menu.tag */
   public String getId() {
      if (m_path!=null && m_path.length>0) {
         return m_path[0];
      } else {
         return null;
      }
   }
   @Override
   public void validate(ActionContext<?> ctx) {
      if (m_action == null) {
         m_action = Action.VIEW;
      }
   }
}

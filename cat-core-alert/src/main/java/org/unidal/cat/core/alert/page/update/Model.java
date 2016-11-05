package org.unidal.cat.core.alert.page.update;

import org.unidal.cat.core.alert.page.AlertPage;
import org.unidal.cat.core.config.page.CoreConfigModel;

public class Model extends CoreConfigModel<AlertPage, Action, Context> {
   private String m_content;

   public Model(Context ctx) {
      super(ctx.getPayload().getName(), ctx);
   }

   public String getContent() {
      return m_content;
   }

   @Override
   public Action getDefaultAction() {
      return Action.VIEW;
   }

   public void setContent(String content) {
      m_content = content;
   }
}

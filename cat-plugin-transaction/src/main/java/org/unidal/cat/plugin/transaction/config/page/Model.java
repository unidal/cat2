package org.unidal.cat.plugin.transaction.config.page;

import org.unidal.cat.core.config.CoreConfigModel;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.config.ConfigPage;

public class Model extends CoreConfigModel<ConfigPage, Action, Context> {
   private String m_content;

   public Model(Context ctx) {
      super(TransactionConstants.NAME, ctx);
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

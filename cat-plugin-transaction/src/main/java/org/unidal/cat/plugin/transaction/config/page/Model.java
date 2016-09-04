package org.unidal.cat.plugin.transaction.config.page;

import org.unidal.cat.core.config.CoreConfigModel;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.config.ConfigPage;

public class Model extends CoreConfigModel<ConfigPage, Action, Context> {
   public Model(Context ctx) {
      super(TransactionConstants.NAME, ctx);
   }

   @Override
   public Action getDefaultAction() {
      return Action.VIEW;
   }
}

package org.unidal.cat.plugin.transaction.config.page;

import org.unidal.cat.plugin.transaction.config.ConfigPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ConfigPage, Action, Context, Model> {
   @Override
   protected String getJspFilePath(Context ctx, Model model) {
      Action action = model.getAction();

      switch (action) {
      case VIEW:
      case EDIT:
         return JspFile.VIEW.getPath();
      default:
         break;
      }

      throw new RuntimeException("Unknown action: " + action);
   }
}

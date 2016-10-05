package org.unidal.cat.core.alert.page.service;

import org.unidal.cat.core.alert.page.AlertPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<AlertPage, Action, Context, Model> {
   @Override
   protected String getJspFilePath(Context ctx, Model model) {
      Action action = model.getAction();

      switch (action) {
      case TEXT:
         return JspFile.VIEW.getPath();
      default:
         break;
      }

      throw new RuntimeException("Unknown action: " + action);
   }
}

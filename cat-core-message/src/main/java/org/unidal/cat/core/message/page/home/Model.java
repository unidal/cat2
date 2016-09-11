package org.unidal.cat.core.message.page.home;

import org.unidal.cat.core.message.page.MessagePage;
import org.unidal.cat.core.report.page.CoreReportModel;
import org.unidal.cat.spi.Report;

public class Model extends CoreReportModel<MessagePage, Action, Context> {
   public Model(Context ctx) {
      super("", ctx);
   }

   @Override
   public Action getDefaultAction() {
      return Action.VIEW;
   }

   @Override
   public String getDomain() {
      return null;
   }

   @Override
   public Report getReport() {
      return null;
   }
}

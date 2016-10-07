package org.unidal.cat.core.alert.page.service;

import org.unidal.cat.core.alert.model.entity.AlertReport;
import org.unidal.cat.core.alert.page.AlertPage;
import org.unidal.web.mvc.ViewModel;
import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

@ModelMeta("alert")
public class Model extends ViewModel<AlertPage, Action, Context> {
   @EntityMeta("report")
   private AlertReport m_report;

   public Model(Context ctx) {
      super(ctx);
   }

   @Override
   public Action getDefaultAction() {
      return Action.TEXT;
   }

   public void setReport(AlertReport report) {
      m_report = report;
   }

   public AlertReport getReport() {
      return m_report;
   }
}

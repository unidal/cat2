package org.unidal.cat.core.report.page;

import org.unidal.cat.spi.Report;
import org.unidal.web.mvc.Action;
import org.unidal.web.mvc.Page;
import org.unidal.web.mvc.ViewModel;

public abstract class CoreReportModel<P extends Page, A extends Action, M extends CoreReportContext<?>> extends
      ViewModel<P, A, M> {
   private transient String m_id;

   public CoreReportModel(String id, M ctx) {
      super(ctx);

      m_id = id;
   }

   public abstract String getDomain();

   public String getId() {
      return m_id;
   }

   /* used by report-navbar.tag */
   public abstract Report getReport();
}

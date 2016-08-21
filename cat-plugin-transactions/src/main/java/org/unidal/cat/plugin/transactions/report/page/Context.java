package org.unidal.cat.plugin.transactions.report.page;

import org.unidal.cat.core.report.CoreReportContext;
import org.unidal.cat.core.report.nav.DomainGroupBar;
import org.unidal.cat.core.report.nav.GroupBar;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;

public class Context extends CoreReportContext<Payload> {
   private DomainGroupBar m_groupBar;

   @Override
   public Query getQuery() {
      return new Query(getHttpServletRequest(), true);
   }

   @Override
   public GroupBar getGroupBar() {
      return m_groupBar;
   }

   protected void setReport(TransactionsReport report) {
      if (report != null) {
         Payload payload = getPayload();

         m_groupBar = lookup(DomainGroupBar.class);
         m_groupBar.initialize(report.getDomain(), payload.getGroup(), "bu", payload.getBu(), report.getBus());
      }
   }
}
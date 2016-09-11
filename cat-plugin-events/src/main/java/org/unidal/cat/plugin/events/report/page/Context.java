package org.unidal.cat.plugin.events.report.page;

import org.unidal.cat.core.report.nav.DomainGroupBar;
import org.unidal.cat.core.report.nav.GroupBar;
import org.unidal.cat.core.report.page.CoreReportContext;
import org.unidal.cat.plugin.events.model.entity.EventsReport;

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

   protected void setReport(EventsReport report) {
      if (report != null) {
         Payload payload = getPayload();

         m_groupBar = lookup(DomainGroupBar.class);
         m_groupBar.initialize(report.getDomain(), payload.getGroup(), "bu", payload.getBu(), report.getBus());
      }
   }
}
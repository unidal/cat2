package org.unidal.cat.plugin.transactions.report.page;

import org.unidal.cat.core.report.CoreReportContext;
import org.unidal.cat.plugin.transaction.report.page.Payload;

public class Context extends CoreReportContext<Payload> {
   @Override
   public Query getQuery() {
      return new Query(getHttpServletRequest(), true);
   }
}
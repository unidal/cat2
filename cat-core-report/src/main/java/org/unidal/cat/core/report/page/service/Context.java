package org.unidal.cat.core.report.page.service;

import org.unidal.cat.core.report.CoreReportContext;
import org.unidal.cat.core.report.nav.GroupBar;

public class Context extends CoreReportContext<Payload> {
   @Override
   public GroupBar getGroupBar() {
      return null; // NOT USED
   }
}

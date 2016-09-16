package org.unidal.cat.core.message.page.home;

import org.unidal.cat.core.report.nav.GroupBar;
import org.unidal.cat.core.report.page.CoreReportContext;

public class Context extends CoreReportContext<Payload> {

   @Override
   public GroupBar getGroupBar() {
      return null;
   }
}

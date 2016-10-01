package org.unidal.cat.core.alert.model;

import org.unidal.cat.core.alert.AlertConstants;
import org.unidal.cat.core.alert.model.entity.AlertReport;
import org.unidal.cat.spi.report.ReportManager;
import org.unidal.cat.spi.report.internals.AbstractReportManager;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportManager.class, value = AlertConstants.NAME)
public class AlertReportManager extends AbstractReportManager<AlertReport> {
   @Override
   public int getThreadsCount() {
      return 1;
   }
}

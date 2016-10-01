package org.unidal.cat.core.alert.model;

import java.util.Collection;

import org.unidal.cat.core.alert.AlertConstants;
import org.unidal.cat.core.alert.model.entity.AlertReport;
import org.unidal.cat.core.alert.model.transform.DefaultMerger;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportAggregator.class, value = AlertConstants.NAME)
public class AlertReportAggregator extends ContainerHolder implements ReportAggregator<AlertReport> {
   @Override
   public AlertReport aggregate(ReportPeriod period, Collection<AlertReport> reports) {
      AlertReport aggregated = new AlertReport();

      if (reports.size() > 0) {
         DefaultMerger merger = new DefaultMerger();

         // must be same domain
         aggregated.setDomain(reports.iterator().next().getDomain());

         for (AlertReport report : reports) {
            report.accept(merger);
         }
      }

      return aggregated;
   }
}

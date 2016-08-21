package org.unidal.cat.plugin.transaction.model;

import java.util.Collection;

import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.filter.TransactionHelper;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportAggregator.class, value = TransactionConstants.NAME)
public class TransactionReportAggregator extends ContainerHolder implements ReportAggregator<TransactionReport> {
   @Inject
   private TransactionHelper m_helper;

   @Override
   public TransactionReport aggregate(ReportPeriod period, Collection<TransactionReport> reports) {
      TransactionReport aggregated = new TransactionReport();

      if (reports.size() > 0) {
         TransactionReportMerger merger = new TransactionReportMerger(aggregated);

         // must be same domain
         aggregated.setDomain(reports.iterator().next().getDomain());

         for (TransactionReport report : reports) {
            report.accept(merger);
         }
      }

      return aggregated;
   }
}

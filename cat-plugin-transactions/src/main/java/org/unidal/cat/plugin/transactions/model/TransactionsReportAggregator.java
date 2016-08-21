package org.unidal.cat.plugin.transactions.model;

import java.util.Collection;

import org.unidal.cat.plugin.transactions.TransactionsConstants;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportAggregator.class, value = TransactionsConstants.NAME)
public class TransactionsReportAggregator extends ContainerHolder implements ReportAggregator<TransactionsReport> {
   @Override
   public TransactionsReport aggregate(ReportPeriod period, Collection<TransactionsReport> reports) {
      TransactionsReport aggregated = new TransactionsReport().setPeriod(period);

      if (reports.size() > 0) {
         TransactionsReportMerger merger = new TransactionsReportMerger(aggregated);

         for (TransactionsReport report : reports) {
            report.accept(merger);
         }
      }

      return aggregated;
   }

   @Override
   public TransactionsReport makeAll(ReportPeriod period, Collection<TransactionsReport> reports) {
      return null;
   }
}

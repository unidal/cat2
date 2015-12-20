package org.unidal.cat.transaction.report;

import java.util.Collection;

import org.unidal.cat.report.ReportPeriod;
import org.unidal.cat.report.spi.ReportAggregator;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

@Named(type = ReportAggregator.class, value = TransactionConstants.ID)
public class TransactionReportAggregator implements ReportAggregator<TransactionReport> {
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

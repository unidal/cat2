package org.unidal.cat.plugin.transaction;

import java.util.Collection;

import com.dianping.cat.Constants;
import com.dianping.cat.service.ProjectService;
import org.unidal.cat.plugin.transaction.filter.TransactionReportHelper;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

@Named(type = ReportAggregator.class, value = TransactionConstants.NAME)
public class TransactionReportAggregator implements ReportAggregator<TransactionReport> {
	@Inject
	private ProjectService m_projectService;

	@Inject
	private TransactionReportHelper m_helper;

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

	@Override
	public TransactionReport makeAll(ReportPeriod period, Collection<TransactionReport> reports) {
		TransactionReport all = new TransactionReport();

		if (reports.size() > 0) {
			all.setDomain(Constants.ALL);
			all.setStartTime(reports.iterator().next().getStartTime());
			all.setEndTime(reports.iterator().next().getEndTime());
			all.setPeriod(reports.iterator().next().getPeriod());

			TransactionAllReportMaker maker = new TransactionAllReportMaker(all, m_projectService, m_helper);

			for (TransactionReport report : reports) {
				report.accept(maker);
			}
		}

		return all;
	}
}

package org.unidal.cat.plugin.transaction.model;

import java.util.Collection;

import org.unidal.cat.plugin.transaction.TransactionConfigProvider;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.filter.TransactionHelper;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;
import com.dianping.cat.service.ProjectService;

@Named(type = ReportAggregator.class, value = TransactionConstants.NAME)
public class TransactionReportAggregator extends ContainerHolder implements ReportAggregator<TransactionReport> {
	@Inject
	private TransactionHelper m_helper;

	private TransactionConfigProvider m_transactionConfigProvider;

	private ProjectService m_projectService;

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

			if (m_projectService == null) {
				m_projectService = lookup(ProjectService.class);
			}

			if (m_transactionConfigProvider == null) {
				m_transactionConfigProvider = lookup(TransactionConfigProvider.class);
			}
		}

		return all;
	}
}

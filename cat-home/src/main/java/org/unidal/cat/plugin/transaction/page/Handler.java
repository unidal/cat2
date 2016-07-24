package org.unidal.cat.plugin.transaction.page;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.filter.TransactionAllNameFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionAllNameGraphFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionAllTypeFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionAllTypeGraphFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionNameFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionNameGraphFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionTypeFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionTypeGraphFilter;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.page.transform.AllReportDistributionBuilder;
import org.unidal.cat.plugin.transaction.view.GraphViewModel;
import org.unidal.cat.plugin.transaction.view.NameViewModel;
import org.unidal.cat.plugin.transaction.view.TypeViewModel;
import org.unidal.cat.plugin.transaction.view.svg.GraphBuilder;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;

public class Handler implements PageHandler<Context> {
	@Inject
	private GraphBuilder m_builder;

	@Inject
	private HistoryGraphs m_historyGraph;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private PayloadNormalizer m_normalizer;

	@Inject
	private AllReportDistributionBuilder m_distributionBuilder;

	@Inject(TransactionConstants.NAME)
	private ReportManager<TransactionReport> m_manager;

	private void buildAllReportDistributionInfo(Model model, String type, String name, String ip,
	      TransactionReport report) {
		m_distributionBuilder.buildAllReportDistributionInfo(model, type, name, ip, report);
	}

	private void buildTransactionMetaInfo(Model model, Payload payload, TransactionReport report) {
		String type = payload.getType();
		String sortBy = payload.getSortBy();
		String query = payload.getQueryName();
		String ip = payload.getIpAddress();

		if (!StringUtils.isEmpty(type)) {
			NameViewModel table = new NameViewModel(report, ip, type, query, sortBy);

			model.setTable(table);
		} else {
			model.setTable(new TypeViewModel(report, ip, query, sortBy));
		}
	}

	private void handleHistoryGraph(Model model, Payload payload) throws IOException {
		String filterId;
		if (payload.getDomain().equals(Constants.ALL)) {
			filterId = payload.getName() == null ? TransactionAllTypeGraphFilter.ID : TransactionAllNameGraphFilter.ID;
		} else {
			filterId = payload.getName() == null ? TransactionTypeGraphFilter.ID : TransactionNameGraphFilter.ID;
		}

		ReportPeriod period = payload.getReportPeriod();
		String domain = payload.getDomain();
		Date date = payload.getStartTime();
		TransactionReport current = m_manager.getReport(period, period.getStartTime(date), domain, filterId, //
		      "ip", payload.getIpAddress(), //
		      "type", payload.getType(), //
		      "name", payload.getName());
		TransactionReport last = m_manager.getReport(period, period.getLastStartTime(date), domain, filterId, //
		      "ip", payload.getIpAddress(), //
		      "type", payload.getType(), //
		      "name", payload.getName());
		TransactionReport baseline = m_manager.getReport(period, period.getBaselineStartTime(date), domain, filterId, //
		      "ip", payload.getIpAddress(), //
		      "type", payload.getType(), //
		      "name", payload.getName());

		model.setReport(current);

		if (current != null) {
			String type = payload.getType();
			String name = payload.getName();
			String ip = payload.getIpAddress();

			if (Constants.ALL.equals(payload.getDomain())) {
				buildAllReportDistributionInfo(model, type, name, ip, current);
			}
		}

		m_historyGraph.buildTrend(model, current, last, baseline);
		// m_historyGraph.buildTrendGraph(model, payload);
	}

	private void handleHistoryReport(Model model, Payload payload) throws IOException {
		String filterId;
		if (payload.getDomain().equals(Constants.ALL)) {
			filterId = payload.getType() == null ? TransactionAllTypeFilter.ID : TransactionAllNameFilter.ID;
		} else {
			filterId = payload.getType() == null ? TransactionTypeFilter.ID : TransactionNameFilter.ID;
		}

		ReportPeriod period = payload.getReportPeriod();
		Date startTime = payload.getStartTime();
		TransactionReport report = m_manager.getReport(period, startTime, payload.getDomain(), filterId, //
		      "ip", payload.getIpAddress(), //
		      "type", payload.getType());

		if (report != null) {
			buildTransactionMetaInfo(model, payload, report);
		}

		model.setReport(report);
	}

	private void handleHourlyGraph(Model model, Payload payload) throws IOException {
		String filterId;
		if (payload.getDomain().equals(Constants.ALL)) {
			filterId = payload.getName() == null ? TransactionAllTypeGraphFilter.ID : TransactionAllNameGraphFilter.ID;
		} else {
			filterId = payload.getName() == null ? TransactionTypeGraphFilter.ID : TransactionNameGraphFilter.ID;
		}

		Date startTime = payload.getStartTime();
		TransactionReport report = m_manager.getReport(ReportPeriod.HOUR, startTime, payload.getDomain(), filterId, //
		      "ip", payload.getIpAddress(), //
		      "type", payload.getType(), //
		      "name", payload.getName());

		if (report != null) {
			String type = payload.getType();
			String name = payload.getName();
			String ip = payload.getIpAddress();
			GraphViewModel graph = new GraphViewModel(m_builder, report, ip, type, name);

			if (Constants.ALL.equals(payload.getDomain())) {
				buildAllReportDistributionInfo(model, type, name, ip, report);
			}

			model.setGraph(graph);
		}

		model.setReport(report);
	}

	private void handleHourlyReport(Model model, Payload payload) throws IOException {
		String filterId;

		if (payload.getDomain().equals(Constants.ALL)) {
			filterId = payload.getType() == null ? TransactionAllTypeFilter.ID : TransactionAllNameFilter.ID;
		} else {
			filterId = payload.getType() == null ? TransactionTypeFilter.ID : TransactionNameFilter.ID;
		}

		Date startTime = payload.getStartTime();
		TransactionReport report = m_manager.getReport(ReportPeriod.HOUR, startTime, payload.getDomain(), filterId, //
		      "ip", payload.getIpAddress(), //
		      "type", payload.getType());

		if (report != null) {
			buildTransactionMetaInfo(model, payload, report);
		} else {
			report = new TransactionReport(payload.getDomain());
			report.setPeriod(ReportPeriod.HOUR);
			report.setStartTime(startTime);
		}

		model.setReport(report);
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "t")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "t")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		normalizePayload(model, payload);

		switch (action) {
		case REPORT:
			if (payload.getReportPeriod() == ReportPeriod.HOUR) {
				handleHourlyReport(model, payload);
			} else {
				handleHistoryReport(model, payload);
			}

			break;
		case GRAPH:
			if (payload.getReportPeriod() == ReportPeriod.HOUR) {
				handleHourlyGraph(model, payload);
			} else {
				handleHistoryGraph(model, payload);
			}

			break;
		}

		TransactionReport report = model.getReport();

		if (report != null) {
			Date startTime = report.getStartTime();
			Date endTime = report.getPeriod().getNextStartTime(startTime);

			report.setEndTime(new Date(endTime.getTime() - 1000));
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void normalizePayload(Model model, Payload payload) {
		m_normalizer.normalize(model, payload);

		model.setPage(ReportPage.TRANSACTION);
		model.setAction(payload.getAction());
		model.setQueryName(payload.getQueryName());
	}
}

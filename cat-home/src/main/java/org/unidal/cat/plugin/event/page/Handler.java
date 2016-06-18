package org.unidal.cat.plugin.event.page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.plugin.event.filter.EventAllNameFilter;
import org.unidal.cat.plugin.event.filter.EventAllNameGraphFilter;
import org.unidal.cat.plugin.event.filter.EventAllTypeFilter;
import org.unidal.cat.plugin.event.filter.EventAllTypeGraphFilter;
import org.unidal.cat.plugin.event.filter.EventNameFilter;
import org.unidal.cat.plugin.event.filter.EventNameGraphFilter;
import org.unidal.cat.plugin.event.filter.EventTypeFilter;
import org.unidal.cat.plugin.event.filter.EventTypeGraphFilter;
import org.unidal.cat.plugin.event.page.DisplayNames.EventNameModel;
import org.unidal.cat.plugin.event.page.GraphPayload.FailurePayload;
import org.unidal.cat.plugin.event.page.GraphPayload.HitPayload;
import org.unidal.cat.plugin.event.page.transform.AllReportDistributionBuilder;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.report.graph.svg.GraphBuilder;
import com.dianping.cat.report.page.event.transform.DistributionDetailVisitor;
import com.dianping.cat.report.page.event.transform.PieGraphChartVisitor;

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

    @Inject(EventConstants.NAME)
    private ReportManager<EventReport> m_manager;

    private void buildDistributionInfo(Model model, String type, String name, EventReport report) {
        PieGraphChartVisitor chartVisitor = new PieGraphChartVisitor(type, name);
        DistributionDetailVisitor detailVisitor = new DistributionDetailVisitor(type, name);

        chartVisitor.visitEventReport(report);
        detailVisitor.visitEventReport(report);
        model.setDistributionChart(chartVisitor.getPieChart().getJsonString());
        model.setDistributionDetails(detailVisitor.getDetails());
    }

    private void buildAllReportDistributionInfo(Model model, String type, String name, String ip, EventReport report) {
        m_distributionBuilder.buildAllReportDistributionInfo(model, type, name, ip, report);
    }

    private void buildEventMetaInfo(Model model, Payload payload, EventReport report) {
        String type = payload.getType();
        String sorted = payload.getSortBy();
        String queryName = payload.getQueryName();
        String ip = payload.getIpAddress();

        if (!StringUtils.isEmpty(type)) {
            DisplayNames displayNames = new DisplayNames();

            model.setDisplayNameReport(displayNames.display(sorted, type, ip, report, queryName));
            buildEventNamePieChart(displayNames.getResults(), model);
        } else {
            model.setDisplayTypeReport(new DisplayTypes().display(sorted, ip, report));
        }
    }

    private void buildEventNameGraph(Model model, EventReport report, String type, String name, String ip) {
        if (name == null || name.length() == 0) {
            name = Constants.ALL;
        }

        EventType t = report.findOrCreateMachine(ip).findOrCreateType(type);
        EventName eventName = t.findOrCreateName(name);

        if (eventName != null) {
            String graph1 = m_builder.build(new HitPayload("Hits Over Time", "Time (min)", "Count", eventName));
            String graph2 = m_builder.build(new FailurePayload("Failures Over Time", "Time (min)", "Count",
                    eventName));

            model.setGraph1(graph1);
            model.setGraph2(graph2);
        }
    }

    private void buildEventNamePieChart(List<EventNameModel> names, Model model) {
        PieChart chart = new PieChart();
        List<Item> items = new ArrayList<Item>();

        for (int i = 1; i < names.size(); i++) {
            EventNameModel name = names.get(i);
            Item item = new Item();
            EventName eventName = name.getDetail();
            item.setNumber(eventName.getTotalCount()).setTitle(eventName.getId());
            items.add(item);
        }

        chart.addItems(items);
        model.setPieChart(new JsonBuilder().toJson(chart));
    }


    private void handleHourlyReport(Model model, Payload payload) throws IOException {
        String filterId;
        if(payload.getDomain().equals(Constants.ALL)){
            filterId = payload.getType() == null ? EventAllTypeFilter.ID : EventAllNameFilter.ID;
        } else {
            filterId = payload.getType() == null ? EventTypeFilter.ID : EventNameFilter.ID;
        }

        Date startTime = payload.getStartTime();
        EventReport report = m_manager.getReport(ReportPeriod.HOUR, startTime, payload.getDomain(), filterId, //
                "ip", payload.getIpAddress(), //
                "type", payload.getType());

        if (report != null) {
            buildEventMetaInfo(model, payload, report);
        } else {
            report = new EventReport(payload.getDomain());
            report.setPeriod(ReportPeriod.HOUR);
            report.setStartTime(startTime);
        }

        model.setReport(report);
    }

    private void handleHourlyGraph(Model model, Payload payload) throws IOException {
        String filterId;
        if(payload.getDomain().equals(Constants.ALL)){
            filterId = payload.getName() == null ? EventAllTypeGraphFilter.ID : EventAllNameGraphFilter.ID;
        } else {
            filterId = payload.getName() == null ? EventTypeGraphFilter.ID : EventNameGraphFilter.ID;
        }

        Date startTime = payload.getStartTime();
        EventReport report = m_manager.getReport(ReportPeriod.HOUR, startTime, payload.getDomain(), filterId, //
                "ip", payload.getIpAddress(), //
                "type", payload.getType(), //
                "name", payload.getName());

        if (report != null) {
            String type = payload.getType();
            String name = payload.getName();
            String ip = payload.getIpAddress();

            if (Constants.ALL.equalsIgnoreCase(ip)) {
                buildDistributionInfo(model, type, name, report);
            } else if (Constants.ALL.equals(payload.getDomain())) {
                buildAllReportDistributionInfo(model, type, name, ip, report);
            }

            buildEventNameGraph(model, report, type, name, ip);
        }

        model.setReport(report);
    }

    private void handleHistoryReport(Model model, Payload payload) throws IOException {
        String filterId;
        if(payload.getDomain().equals(Constants.ALL)){
            filterId = payload.getType() == null ? EventAllTypeFilter.ID : EventAllNameFilter.ID;
        } else {
            filterId = payload.getType() == null ? EventTypeFilter.ID : EventNameFilter.ID;
        }

        ReportPeriod period = payload.getReportPeriod();
        Date startTime = payload.getStartTime();
        EventReport report = m_manager.getReport(period, startTime, payload.getDomain(), filterId, //
                "ip", payload.getIpAddress(), //
                "type", payload.getType());

        if (report != null) {
            buildEventMetaInfo(model, payload, report);
        }

        model.setReport(report);
    }

    private void handleHistoryGraph(Model model, Payload payload) throws IOException {
        String filterId;
        if(payload.getDomain().equals(Constants.ALL)){
            filterId = payload.getName() == null ? EventAllTypeGraphFilter.ID : EventAllNameGraphFilter.ID;
        } else {
            filterId = payload.getName() == null ? EventTypeGraphFilter.ID : EventNameGraphFilter.ID;
        }

        ReportPeriod period = payload.getReportPeriod();
        String domain = payload.getDomain();
        Date date = payload.getStartTime();
        EventReport current = m_manager.getReport(period, period.getStartTime(date), domain, filterId, //
                "ip", payload.getIpAddress(), //
                "type", payload.getType(), //
                "name", payload.getName());
        EventReport last = m_manager.getReport(period, period.getLastStartTime(date), domain, filterId, //
                "ip", payload.getIpAddress(), //
                "type", payload.getType(), //
                "name", payload.getName());
        EventReport baseline = m_manager.getReport(period, period.getBaselineStartTime(date), domain, filterId, //
                "ip", payload.getIpAddress(), //
                "type", payload.getType(), //
                "name", payload.getName());

        model.setReport(current);

        if (current != null) {
            String type = payload.getType();
            String name = payload.getName();
            String ip = payload.getIpAddress();

            if (Constants.ALL.equalsIgnoreCase(ip)) {
                buildDistributionInfo(model, type, name, current);
            } else if (Constants.ALL.equals(payload.getDomain())) {
                buildAllReportDistributionInfo(model, type, name, ip, current);
            }
        }

        m_historyGraph.buildTrend(model, current, last, baseline);
        // m_historyGraph.buildTrendGraph(model, payload);
    }

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "e")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "e")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
        Action action = payload.getAction();

		normalize(model, payload);

		switch (action) {
            case HOURLY_REPORT:
                handleHourlyReport(model, payload);
                break;
            case HOURLY_GRAPH:
                handleHourlyGraph(model, payload);
                break;
            case HISTORY_REPORT:
                handleHistoryReport(model, payload);
                break;
            case HISTORY_GRAPH:
                handleHistoryGraph(model, payload);
                break;
		}

        EventReport report = model.getReport();
        Date startTime = report.getStartTime();

        // TODO for history report, endTime should not be startTime + HOUR
        Date endTime = ReportPeriod.HOUR.getNextStartTime(startTime);

        report.setEndTime(new Date(endTime.getTime() - 1000));

        if (!ctx.isProcessStopped()) {
            m_jspViewer.view(ctx, model);
        }
	}

	private void normalize(Model model, Payload payload) {
        m_normalizer.normalize(model, payload);

        model.setPage(ReportPage.EVENT);
        model.setAction(payload.getAction());
        model.setQueryName(payload.getQueryName());
	}

	public enum DetailOrder {
		TYPE, NAME, TOTAL_COUNT, FAILURE_COUNT
	}

	public enum SummaryOrder {
		TYPE, TOTAL_COUNT, FAILURE_COUNT
	}
}

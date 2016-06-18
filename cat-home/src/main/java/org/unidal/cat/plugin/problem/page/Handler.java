package org.unidal.cat.plugin.problem.page;

import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.problem.transform.DetailStatistics;
import com.dianping.cat.report.page.problem.transform.HourlyLineChartVisitor;
import com.dianping.cat.report.page.problem.transform.PieGraphChartVisitor;
import com.dianping.cat.report.page.problem.transform.ProblemStatistics;
import com.dianping.cat.report.service.ModelPeriod;
import org.unidal.cat.plugin.problem.ProblemConstants;
import org.unidal.cat.plugin.problem.filter.ProblemDetailFilter;
import org.unidal.cat.plugin.problem.filter.ProblemGraphFilter;
import org.unidal.cat.plugin.problem.filter.ProblemHomePageFilter;
import org.unidal.cat.plugin.problem.filter.ProblemThreadFilter;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import javax.servlet.ServletException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class Handler implements PageHandler<Context> {
    @Inject
    private HistoryGraphs m_historyGraphs;

    @Inject
    private JspViewer m_jspViewer;

    @Inject
    private ServerConfigManager m_serverConfigManager;

    @Inject
    private DomainGroupConfigManager m_configManager;

    @Inject
    private PayloadNormalizer m_normalizePayload;

    @Inject
    private JsonBuilder m_jsonBuilder;

    @Inject(ProblemConstants.NAME)
    private ReportManager<ProblemReport> m_manager;

    private void buildDistributionChart(Model model, Payload payload, ProblemReport report) {
        if (payload.getIpAddress().equalsIgnoreCase(Constants.ALL)) {
            PieGraphChartVisitor pieChart = new PieGraphChartVisitor(payload.getType(), payload.getStatus());

            pieChart.visitProblemReport(report);
            model.setDistributionChart(pieChart.getPieChart().getJsonString());
        }
    }

    private int getHour(long date) {
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(date);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    @Override
    @PayloadMeta(Payload.class)
    @InboundActionMeta(name = "p")
    public void handleInbound(Context ctx) throws ServletException, IOException {
        // display only, no action here
    }

    @Override
    @OutboundActionMeta(name = "p")
    public void handleOutbound(Context ctx) throws ServletException, IOException {
        Model model = new Model(ctx);
        Payload payload = ctx.getPayload();
        normalize(model, payload);

        ProblemReport report;
        ProblemStatistics problemStatistics = new ProblemStatistics();
        LongConfig longConfig = new LongConfig();
        Action action = payload.getAction();

        longConfig.setSqlThreshold(payload.getSqlThreshold()).setUrlThreshold(payload.getUrlThreshold())
                .setServiceThreshold(payload.getServiceThreshold());
        longConfig.setCacheThreshold(payload.getCacheThreshold()).setCallThreshold(payload.getCallThreshold());
        problemStatistics.setLongConfig(longConfig);

        switch (action) {
            case HOURLY_REPORT:
                handleHourlyReport(model, payload);
                break;
            case HISTORY_REPORT:
                handleHistoryReport(model, payload);
                break;
            case HISTORY_GRAPH:
                handleHistoryGraph(model, payload);
                break;
            case HOUR_GRAPH:
                handleHourlyGraph(model, payload);
                break;
            case GROUP:
                handleGroupReport(model, payload);
                break;
            case THREAD:
                handleThreadReport(model, payload);
                break;
            case DETAIL:
                handleDetailReport(model, payload);
                break;
        }

        report = model.getReport();

        if (report != null) {
            Date startTime = report.getStartTime();

            // TODO for history report, endTime should not be startTime + HOUR
            Date endTime = ReportPeriod.HOUR.getNextStartTime(startTime);

            report.setEndTime(new Date(endTime.getTime() - 1000));
        }

        if (!ctx.isProcessStopped()) {
            m_jspViewer.view(ctx, model);
        }
    }

    private void handleDetailReport(Model model, Payload payload) throws IOException {
        String ip = payload.getIpAddress();
        model.setDate(payload.getDate());
        model.setIpAddress(ip);
        model.setGroupName(payload.getGroupName());
        model.setCurrentMinute(payload.getMinute());
        model.setThreadId(payload.getThreadId());

        Date startTime = payload.getStartTime();
        String filterId = ProblemDetailFilter.ID;

        ProblemReport report = m_manager.getReport(ReportPeriod.HOUR, startTime, payload.getDomain(), filterId,
                "ip", ip,
                "group", payload.getGroup());

        if (report == null) {
            return;
        }
        model.setReport(report);
        DetailStatistics detail = new DetailStatistics();
        detail.setIp(ip).setMinute(payload.getMinute());
        detail.setGroup(payload.getGroup()).setThreadId(payload.getThreadId());
        detail.visitProblemReport(report);
        model.setDetailStatistics(detail);
    }

    private void handleThreadReport(Model model, Payload payload) throws IOException {
        ProblemReport report = showHourlyReport(model, payload);
        String groupName = payload.getGroupName();
        model.setGroupName(groupName);
        if (report != null) {
            model.setThreadLevelInfo(new ThreadLevelInfo(model, groupName).display(report));
        }
    }

    private void handleGroupReport(Model model, Payload payload) throws IOException {
        ProblemReport report = showHourlyReport(model, payload);
        if (report != null) {
            model.setGroupLevelInfo(new GroupLevelInfo(model).display(report));
        }
    }

    private void handleHourlyGraph(Model model, Payload payload) throws IOException {
        String type = payload.getType();
        String status = payload.getStatus();
        String ip = payload.getIpAddress();
        Date startTime = payload.getStartTime();
        String filterId = ProblemGraphFilter.ID;

        ProblemReport report = m_manager.getReport(ReportPeriod.HOUR, startTime, payload.getDomain(), filterId, //
                "ip", payload.getIpAddress(),
                "type", type,
                "status", status);

        if (report != null) {
            Date start = report.getStartTime();
            HourlyLineChartVisitor vistor = new HourlyLineChartVisitor(ip, type, status, start);

            vistor.visitProblemReport(report);
            model.setErrorsTrend(m_jsonBuilder.toJson(vistor.getGraphItem()));
            buildDistributionChart(model, payload, report);
        } else {
            report = new ProblemReport(payload.getDomain());
            report.setPeriod(ReportPeriod.HOUR);
            report.setStartTime(startTime);
        }
        model.setReport(report);
    }

    private void handleHistoryGraph(Model model, Payload payload) throws IOException {
        m_historyGraphs.buildTrendGraph(model, payload);
        String filterId = ProblemGraphFilter.ID;

        Date startTime = payload.getStartTime();
        ReportPeriod period = payload.getReportPeriod();
        ProblemReport report = m_manager.getReport(period, startTime, payload.getDomain(), filterId, //
                "ip", payload.getIpAddress());

        if (report != null) {
            buildDistributionChart(model, payload, report);
        }
    }

    private void handleHistoryReport(Model model, Payload payload) throws IOException {
        String ip = model.getIpAddress();
        String filterId = ProblemHomePageFilter.ID;

        Date startTime = payload.getStartTime();
        ReportPeriod period = payload.getReportPeriod();
        ProblemReport report = m_manager.getReport(period, startTime, payload.getDomain(), filterId, //
                "ip", payload.getIpAddress());

        if (report != null) {
            ProblemStatistics problemStatistics = getProblemStatistics(payload);

            if (ip.equals(Constants.ALL)) {
                problemStatistics.setAllIp(true);
            } else {
                problemStatistics.setIp(ip);
            }
            problemStatistics.visitProblemReport(report);
            model.setAllStatistics(problemStatistics);
        } else {
            report = new ProblemReport(payload.getDomain());
            report.setPeriod(period);
            report.setStartTime(startTime);
        }
        model.setReport(report);
    }

    private void handleHourlyReport(Model model, Payload payload) throws IOException {
        String ip = model.getIpAddress();
        String filterId = ProblemHomePageFilter.ID;

        Date startTime = payload.getStartTime();
        ProblemReport report = m_manager.getReport(ReportPeriod.HOUR, startTime, payload.getDomain(), filterId, //
                "ip", payload.getIpAddress());

        if (report != null) {
            ProblemStatistics problemStatistics = getProblemStatistics(payload);

            if (ip.equals(Constants.ALL)) {
                problemStatistics.setAllIp(true);
            } else {
                problemStatistics.setIp(ip);
            }
            problemStatistics.visitProblemReport(report);
            model.setAllStatistics(problemStatistics);
        } else {
            report = new ProblemReport(payload.getDomain());
            report.setPeriod(ReportPeriod.HOUR);
            report.setStartTime(startTime);
        }
        model.setReport(report);
    }

    private ProblemStatistics getProblemStatistics(Payload payload) {
        ProblemStatistics problemStatistics = new ProblemStatistics();
        LongConfig longConfig = new LongConfig();

        longConfig.setSqlThreshold(payload.getSqlThreshold()).setUrlThreshold(payload.getUrlThreshold())
                .setServiceThreshold(payload.getServiceThreshold());
        longConfig.setCacheThreshold(payload.getCacheThreshold()).setCallThreshold(payload.getCallThreshold());
        problemStatistics.setLongConfig(longConfig);
        return problemStatistics;
    }

    private void normalize(Model model, Payload payload) {
        setDefaultThreshold(model, payload);
        model.setPage(ReportPage.PROBLEM);
        model.setAction(payload.getAction());
        m_normalizePayload.normalize(model, payload);
    }

    private void setDefaultThreshold(Model model, Payload payload) {
        Map<String, Domain> domains = m_serverConfigManager.getLongConfigDomains();
        Domain d = domains.get(payload.getDomain());

        if (d != null) {
            int longUrlTime = d.getUrlThreshold() == null ? m_serverConfigManager.getLongUrlDefaultThreshold() : d.getUrlThreshold();

            if (longUrlTime != 500 && longUrlTime != 1000 && longUrlTime != 2000 && longUrlTime != 3000
                    && longUrlTime != 4000 && longUrlTime != 5000) {
                double sec = (double) (longUrlTime) / (double) 1000;
                NumberFormat nf = new DecimalFormat("#.##");
                String option = "<option value=\"" + longUrlTime + "\"" + ">" + nf.format(sec) + " Sec</option>";

                model.setDefaultThreshold(option);
            }

            int longSqlTime = d.getSqlThreshold();

            if (longSqlTime != 100 && longSqlTime != 500 && longSqlTime != 1000) {
                double sec = (double) (longSqlTime);
                NumberFormat nf = new DecimalFormat("#");
                String option = "<option value=\"" + longSqlTime + "\"" + ">" + nf.format(sec) + " ms</option>";

                model.setDefaultSqlThreshold(option);
            }
        }
    }

    private ProblemReport showHourlyReport(Model model, Payload payload) throws IOException {
        ModelPeriod period = payload.getPeriod();

        model.setDate(payload.getDate());

        if (period.isCurrent()) {
            Calendar cal = Calendar.getInstance();
            int minute = cal.get(Calendar.MINUTE);

            model.setLastMinute(minute);
        } else {
            model.setLastMinute(59);
        }
        model.setHour(getHour(model.getLongDate()));

        Date startTime = payload.getStartTime();
        String ip = payload.getIpAddress();
        String filterId = ProblemThreadFilter.ID;

        ProblemReport report = m_manager.getReport(ReportPeriod.HOUR, startTime, payload.getDomain(), filterId, //
                "ip", ip);

        if (report != null) {
            model.setIpAddress(ip);
            model.setReport(report);
        }
        return report;
    }

    public enum DetailOrder {
        TYPE, STATUS, TOTAL_COUNT, DETAIL
    }

    public enum SummaryOrder {
        TYPE, TOTAL_COUNT, DETAIL
    }
}

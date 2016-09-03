package org.unidal.cat.plugin.event.report.page;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.unidal.cat.core.view.svg.GraphBuilder;
import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.plugin.event.filter.EventNameFilter;
import org.unidal.cat.plugin.event.filter.EventNameGraphFilter;
import org.unidal.cat.plugin.event.filter.EventTypeFilter;
import org.unidal.cat.plugin.event.filter.EventTypeGraphFilter;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.plugin.event.report.view.GraphViewModel;
import org.unidal.cat.plugin.event.report.view.NameViewModel;
import org.unidal.cat.plugin.event.report.view.TypeViewModel;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.mvc.PayloadNormalizer;

public class Handler implements PageHandler<Context> {
   @Inject
   private GraphBuilder m_builder;

   @Inject
   private JspViewer m_jspViewer;

   @Inject
   private PayloadNormalizer m_normalizer;

   @Inject(EventConstants.NAME)
   private ReportManager<EventReport> m_manager;

   private void handleHistoryGraph(Model model, Payload payload) throws IOException {
      ReportPeriod period = payload.getPeriod();
      Date startTime = period.getStartTime(payload.getStartTime());
      String domain = payload.getDomain();
      String group = payload.getGroup();
      String ip = payload.getIp();
      String type = payload.getType();
      String name = payload.getName();
      String filterId = (name == null ? EventTypeGraphFilter.ID : EventNameGraphFilter.ID);

      EventReport current = m_manager.getReport(period, startTime, domain, filterId, //
            "group", group, "ip", ip, "type", type, "name", name);
      EventReport last = m_manager.getReport(period, startTime, domain, filterId, //
            "group", group, "ip", ip, "type", type, "name", name);
      EventReport base = m_manager.getReport(period, startTime, domain, filterId, //
            "group", group, "ip", ip, "type", type, "name", name);
      GraphViewModel graph = new GraphViewModel(ip, type, name, current, last, base);

      model.setGraph(graph);
      model.setReport(current);
   }

   private void handleHistoryReport(Model model, Payload payload) throws IOException {
      Date startTime = payload.getStartTime();
      String domain = payload.getDomain();
      String group = payload.getGroup();
      String ip = payload.getIp();
      String type = payload.getType();
      String sortBy = payload.getSortBy();
      String query = payload.getQuery();
      String filterId = (type == null ? EventTypeFilter.ID : EventNameFilter.ID);

      ReportPeriod period = payload.getPeriod();
      EventReport report = m_manager.getReport(period, startTime, domain, filterId, //
            "group", group, "ip", ip, "type", type);

      if (report != null) {
         if (type != null) {
            model.setTable(new NameViewModel(report, ip, type, query, sortBy));
         } else {
            model.setTable(new TypeViewModel(report, ip, query, sortBy));
         }
      } else {
         report = new EventReport(domain);
         report.setPeriod(period);
         report.setStartTime(startTime);
      }

      model.setReport(report);
   }

   private void handleHourlyGraph(Model model, Payload payload) throws IOException {
      Date startTime = payload.getStartTime();
      String domain = payload.getDomain();
      String group = payload.getGroup();
      String ip = payload.getIp();
      String type = payload.getType();
      String name = payload.getName();
      String filterId = (name == null ? EventTypeGraphFilter.ID : EventNameGraphFilter.ID);

      EventReport report = m_manager.getReport(ReportPeriod.HOUR, startTime, domain, filterId, //
            "group", group, "ip", ip, "type", type, "name", name);

      if (report != null) {
         GraphViewModel graph = new GraphViewModel(m_builder, ip, type, name, report);

         model.setGraph(graph);
      }

      model.setReport(report);
   }

   private void handleHourlyReport(Model model, Payload payload) throws IOException {
      Date startTime = payload.getStartTime();
      String domain = payload.getDomain();
      String group = payload.getGroup();
      String ip = payload.getIp();
      String type = payload.getType();
      String sortBy = payload.getSortBy();
      String query = payload.getQuery();
      String filterId = (type == null ? EventTypeFilter.ID : EventNameFilter.ID);

      EventReport report = m_manager.getReport(ReportPeriod.HOUR, startTime, domain, filterId, //
            "group", group, "ip", ip, "type", type);

      if (report != null) {
         if (type != null) {
            model.setTable(new NameViewModel(report, ip, type, query, sortBy));
         } else {
            model.setTable(new TypeViewModel(report, ip, query, sortBy));
         }
      } else {
         report = new EventReport(domain);
         report.setPeriod(ReportPeriod.HOUR);
         report.setStartTime(startTime);
      }

      model.setReport(report);
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

      model.setAction(action);

      switch (action) {
      case REPORT:
         if (payload.getPeriod().isHour()) {
            handleHourlyReport(model, payload);
         } else {
            handleHistoryReport(model, payload);
         }

         break;
      case GRAPH:
         if (payload.getPeriod().isHour()) {
            handleHourlyGraph(model, payload);
         } else {
            handleHistoryGraph(model, payload);
         }

         break;
      }

      EventReport report = model.getReport();

      if (report != null) {
         Date startTime = report.getStartTime();
         Date endTime = report.getPeriod().getNextStartTime(startTime);

         report.setEndTime(new Date(endTime.getTime() - 1000));
         ctx.setReport(report);
      }

      if (!ctx.isProcessStopped()) {
         m_jspViewer.view(ctx, model);
      }
   }
}

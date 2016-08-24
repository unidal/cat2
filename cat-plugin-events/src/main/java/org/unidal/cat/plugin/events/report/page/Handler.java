package org.unidal.cat.plugin.events.report.page;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.unidal.cat.core.report.view.svg.GraphBuilder;
import org.unidal.cat.plugin.events.EventsConstants;
import org.unidal.cat.plugin.events.filter.EventsNameFilter;
import org.unidal.cat.plugin.events.filter.EventsNameGraphFilter;
import org.unidal.cat.plugin.events.filter.EventsTypeFilter;
import org.unidal.cat.plugin.events.filter.EventsTypeGraphFilter;
import org.unidal.cat.plugin.events.model.entity.EventsReport;
import org.unidal.cat.plugin.events.report.view.GraphViewModel;
import org.unidal.cat.plugin.events.report.view.NameViewModel;
import org.unidal.cat.plugin.events.report.view.TypeViewModel;
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

   @Inject(EventsConstants.NAME)
   private ReportManager<EventsReport> m_manager;

   private void handleHistoryGraph(Model model, Payload payload) throws IOException {
   }

   private void handleHistoryReport(Model model, Payload payload) throws IOException {
   }

   private void handleHourlyGraph(Model model, Payload payload) throws IOException {
      Date startTime = payload.getStartTime();
      String domain = payload.getDomain();
      String group = payload.getGroup();
      String bu = payload.getBu();
      String type = payload.getType();
      String name = payload.getName();
      String filterId = (name == null ? EventsTypeGraphFilter.ID : EventsNameGraphFilter.ID);

      EventsReport report = m_manager.getReport(ReportPeriod.HOUR, startTime, domain, filterId, //
            "group", group, "bu", bu, "type", type, "name", name);

      if (report != null) {
         GraphViewModel graph = new GraphViewModel(m_builder, bu, type, name, report);

         model.setGraph(graph);
      }

      model.setReport(report);
   }

   private void handleHourlyReport(Model model, Payload payload) throws IOException {
      Date startTime = payload.getStartTime();
      String domain = payload.getDomain();
      String group = payload.getGroup();
      String bu = payload.getBu();
      String type = payload.getType();
      String sortBy = payload.getSortBy();
      String query = payload.getQuery();
      String filterId = (type == null ? EventsTypeFilter.ID : EventsNameFilter.ID);

      EventsReport report = m_manager.getReport(ReportPeriod.HOUR, startTime, domain, filterId, //
            "group", group, "bu", bu, "type", type);

      if (report != null) {
         if (type != null) {
            model.setTable(new NameViewModel(report, bu, type, query, sortBy));
         } else {
            model.setTable(new TypeViewModel(report, bu, query, sortBy));
         }
      } else {
         report = new EventsReport();
         report.setPeriod(ReportPeriod.HOUR);
         report.setStartTime(startTime);
      }

      model.setReport(report);
   }

   @Override
   @PayloadMeta(Payload.class)
   @InboundActionMeta(name = "es")
   public void handleInbound(Context ctx) throws ServletException, IOException {
      // display only, no action here
   }

   @Override
   @OutboundActionMeta(name = "es")
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

      EventsReport report = model.getReport();

      if (report != null) {
         Date startTime = payload.getStartTime();
         Date endTime = report.getPeriod().getNextStartTime(startTime);

         report.setEndTime(new Date(endTime.getTime() - 1000));
         ctx.setReport(report);
      }

      if (!ctx.isProcessStopped()) {
         m_jspViewer.view(ctx, model);
      }
   }
}

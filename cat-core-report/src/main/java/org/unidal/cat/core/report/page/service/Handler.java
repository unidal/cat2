package org.unidal.cat.core.report.page.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpStatus;
import org.unidal.cat.core.report.page.ReportPage;
import org.unidal.cat.core.report.remote.DefaultRemoteReportContext;
import org.unidal.cat.core.report.remote.RemoteReportContext;
import org.unidal.cat.core.report.remote.RemoteReportSkeleton;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.report.ReportFilter;
import org.unidal.cat.spi.report.ReportFilterManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;

public class Handler implements PageHandler<Context> {
   @Inject
   private RemoteReportSkeleton m_skeleton;

   @Inject
   private ReportFilterManager m_manager;

   @SuppressWarnings("unchecked")
   private RemoteReportContext buildContext(HttpServletRequest req, Payload payload) {
      ReportFilter<Report> filter = m_manager.getFilter(payload.getName(), payload.getFilterId());
      DefaultRemoteReportContext ctx = new DefaultRemoteReportContext(payload.getName(), payload.getDomain(), //
            payload.getStartTime(), payload.getPeriod(), filter);
      List<String> names = Collections.list(req.getParameterNames());

      for (String name : names) {
         String value = req.getParameter(name);
         try {
            value = URLDecoder.decode(value, "UTF-8");
         } catch (UnsupportedEncodingException e) {
            Cat.logError(e);
         }
         ctx.setProperty(name, value);
      }

      return ctx;
   }

   @Override
   @PayloadMeta(Payload.class)
   @InboundActionMeta(name = "service")
   public void handleInbound(Context ctx) throws ServletException, IOException {
      // display only, no action here
   }

   @Override
   @OutboundActionMeta(name = "service")
   public void handleOutbound(Context ctx) throws ServletException, IOException {
      Model model = new Model(ctx);
      Payload payload = ctx.getPayload();

      model.setAction(Action.VIEW);
      model.setPage(ReportPage.SERVICE);

      if (ctx.hasErrors()) {
         ctx.getHttpServletResponse().sendError(HttpStatus.SC_BAD_REQUEST, "Bad Request");
         ctx.stopProcess();
      } else {
         OutputStream out = ctx.getHttpServletResponse().getOutputStream();

         RemoteReportContext rc = buildContext(ctx.getHttpServletRequest(), payload);
         m_skeleton.handleReport(rc, out);
      }
   }
}

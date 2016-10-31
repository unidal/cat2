package org.unidal.cat.core.alert.page.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.unidal.cat.core.alert.model.AlertReportBuilder;
import org.unidal.cat.core.alert.model.entity.AlertReport;
import org.unidal.cat.core.alert.model.transform.DefaultNativeBuilder;
import org.unidal.cat.core.alert.page.AlertPage;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
   @Inject
   private JspViewer m_jspViewer;

   @Inject
   private AlertReportBuilder m_builder;

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
      Action action = payload.getAction();
      AlertReport report = m_builder.build();

      model.setAction(Action.TEXT);
      model.setPage(AlertPage.SERVICE);
      model.setReport(report);

      if (action == Action.BINARY) {
         HttpServletRequest req = ctx.getHttpServletRequest();
         HttpServletResponse res = ctx.getHttpServletResponse();
         String acceptEncoding = req.getHeader("Accept-Encoding");

         // res.setContentType("application/octet-stream");

         if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
            res.setHeader("Content-Encoding", "gzip");

            GZIPOutputStream out = new GZIPOutputStream(res.getOutputStream());

            DefaultNativeBuilder.build(report, out);
            out.finish();
            out.flush();
         } else {
            OutputStream out = res.getOutputStream();

            DefaultNativeBuilder.build(report, out);
            out.flush();
         }

         ctx.stopProcess();
      }

      if (!ctx.isProcessStopped()) {
         m_jspViewer.view(ctx, model);
      }
   }
}

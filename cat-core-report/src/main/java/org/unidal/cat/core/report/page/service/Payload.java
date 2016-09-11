package org.unidal.cat.core.report.page.service;

import java.util.Date;
import java.util.List;

import org.unidal.cat.core.report.page.CoreReportPayload;
import org.unidal.cat.core.report.page.ReportPage;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.PathMeta;

public class Payload extends CoreReportPayload<ReportPage, Action> {
   public Payload() {
      super(ReportPage.SERVICE);
   }

   private ReportPage m_page;

   @FieldMeta("op")
   private Action m_action;

   @PathMeta("path")
   private List<String> m_path;

   private String m_name;

   private String m_domain;

   private ReportPeriod m_period;

   private Date m_startTime;

   private String m_filterId;

   @Override
   public Action getAction() {
      return m_action;
   }

   public String getDomain() {
      return m_domain;
   }

   public String getFilterId() {
      return m_filterId;
   }

   public String getName() {
      return m_name;
   }

   @Override
   public ReportPage getPage() {
      return m_page;
   }

   public ReportPeriod getPeriod() {
      return m_period;
   }

   public Date getStartTime() {
      return m_startTime;
   }

   public void setAction(String action) {
      m_action = Action.getByName(action, Action.VIEW);
   }

   @Override
   public void setPage(String page) {
      m_page = ReportPage.getByName(page, ReportPage.SERVICE);
   }

   @Override
   public void validate(ActionContext<?> ctx) {
      if (m_action == null) {
         m_action = Action.VIEW;
      }

      String name = m_path.size() > 0 ? m_path.get(0) : null;
      String domain = m_path.size() > 1 ? m_path.get(1) : null;
      String period = m_path.size() > 2 ? m_path.get(2) : null;
      String startTime = m_path.size() > 3 ? m_path.get(3) : null;
      String filterId = m_path.size() > 4 ? m_path.get(4) : null;

      m_name = name;
      m_domain = domain;
      m_period = ReportPeriod.getByName(period, null);
      m_startTime = m_period == null ? null : m_period.parse(startTime, null);
      m_filterId = filterId;

      if (m_name == null || m_domain == null || m_period == null | m_startTime == null) {
         ctx.addError("payload.invalid");
      }
   }
}

package org.unidal.cat.plugin.events.report.page;

import java.util.HashMap;
import java.util.Map;

import org.unidal.cat.core.report.CoreReportModel;
import org.unidal.cat.core.view.LineChart;
import org.unidal.cat.core.view.TableViewModel;
import org.unidal.cat.plugin.events.report.view.GraphViewModel;
import org.unidal.cat.plugin.events.EventsConstants;
import org.unidal.cat.plugin.events.model.entity.EventsReport;
import org.unidal.cat.plugin.events.report.ReportPage;
import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

@ModelMeta(EventsConstants.NAME)
public class Model extends CoreReportModel<ReportPage, Action, Context> {
   @EntityMeta
   private EventsReport m_report;

   private TableViewModel<?> m_table;

   private GraphViewModel m_graph;

   private Map<String, LineChart> m_lineCharts = new HashMap<String, LineChart>();

   public Model(Context ctx) {
      super(EventsConstants.NAME, ctx);
   }

   @Override
   public Action getDefaultAction() {
      return Action.REPORT;
   }

   @Override
   public String getDomain() {
      if (m_report == null) {
         return null;
      } else {
         return m_report.getDomain();
      }
   }

   public GraphViewModel getGraph() {
      return m_graph;
   }

   public Map<String, LineChart> getLineCharts() {
      return m_lineCharts;
   }

   @Override
   public EventsReport getReport() {
      return m_report;
   }

   public TableViewModel<?> getTable() {
      return m_table;
   }

   public void setGraph(GraphViewModel graph) {
      m_graph = graph;
   }

   public void setLineChart(String name, LineChart lineChart) {
      m_lineCharts.put(name, lineChart);
   }

   public void setReport(EventsReport report) {
      m_report = report;
   }

   public void setTable(TableViewModel<?> table) {
      m_table = table;
   }
}
package org.unidal.cat.plugin.event.report.page;

import java.util.HashMap;
import java.util.Map;

import org.unidal.cat.core.report.CoreReportModel;
import org.unidal.cat.core.report.view.LineChart;
import org.unidal.cat.core.report.view.TableViewModel;
import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.plugin.event.report.ReportPage;
import org.unidal.cat.plugin.event.report.view.GraphViewModel;
import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

@ModelMeta(EventConstants.NAME)
public class Model extends CoreReportModel<ReportPage, Action, Context> {
   @EntityMeta
   private EventReport m_report;

   private TableViewModel<?> m_table;

   private GraphViewModel m_graph;

   private Map<String, LineChart> m_lineCharts = new HashMap<String, LineChart>();

   public Model(Context ctx) {
      super(EventConstants.NAME, ctx);
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
   public EventReport getReport() {
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

   public void setReport(EventReport report) {
      m_report = report;
   }

   public void setTable(TableViewModel<?> table) {
      m_table = table;
   }
}

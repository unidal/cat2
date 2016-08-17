package org.unidal.cat.plugin.transaction.report.page;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.unidal.cat.core.report.CoreReportModel;
import org.unidal.cat.core.report.view.LineChart;
import org.unidal.cat.core.report.view.TableViewModel;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.report.ReportPage;
import org.unidal.cat.plugin.transaction.view.GraphViewModel;
import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

@ModelMeta(TransactionConstants.NAME)
public class Model extends CoreReportModel<ReportPage, Action, Context> {
   @EntityMeta
   private TransactionReport m_report;

   private TableViewModel<?> m_table;

   private GraphViewModel m_graph;

   private Map<String, LineChart> m_lineCharts = new HashMap<String, LineChart>();

   public Model(Context ctx) {
      super(TransactionConstants.NAME, ctx);
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

   protected Set<String> getItems() {
      if (m_report == null) {
         return Collections.emptySet();
      } else {
         return new HashSet<String>(m_report.getIps());
      }
   }

   public Map<String, LineChart> getLineCharts() {
      return m_lineCharts;
   }

   @Override
   public TransactionReport getReport() {
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

   public void setReport(TransactionReport report) {
      m_report = report;
   }

   public void setTable(TableViewModel<?> table) {
      m_table = table;
   }
}

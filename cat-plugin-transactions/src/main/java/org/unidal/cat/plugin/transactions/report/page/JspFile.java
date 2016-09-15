package org.unidal.cat.plugin.transactions.report.page;

public enum JspFile {
   REPORT("/jsp/report/transactions.jsp"),

   HOURLY_GRAPH("/jsp/report/transactions-hourly-graph.jsp"),

   HISTORY_GRAPH("/jsp/report/transactions-history-graph.jsp");

   private String m_path;

   private JspFile(String path) {
      m_path = path;
   }

   public String getPath() {
      return m_path;
   }
}

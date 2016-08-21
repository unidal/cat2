package org.unidal.cat.plugin.transactions.report.page;

public enum JspFile {
   REPORT("/jsp/plugin/transactions/transactions.jsp"),

   HOURLY_GRAPH("/jsp/plugin/transactions/transactions-hourly-graph.jsp"),

   HISTORY_GRAPH("/jsp/plugin/transactions/transactions-history-graph.jsp");

   private String m_path;

   private JspFile(String path) {
      m_path = path;
   }

   public String getPath() {
      return m_path;
   }
}

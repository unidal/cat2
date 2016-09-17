package org.unidal.cat.plugin.events.report.page;

public enum JspFile {
   REPORT("/jsp/report/events.jsp"),

   HOURLY_GRAPH("/jsp/report/events-hourly-graph.jsp"),

   HISTORY_GRAPH("/jsp/report/events-history-graph.jsp");

   private String m_path;

   private JspFile(String path) {
      m_path = path;
   }

   public String getPath() {
      return m_path;
   }
}

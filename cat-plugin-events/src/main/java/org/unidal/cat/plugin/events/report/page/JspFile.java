package org.unidal.cat.plugin.events.report.page;

public enum JspFile {
   REPORT("/jsp/plugin/events/events.jsp"),

   HOURLY_GRAPH("/jsp/plugin/events/events-hourly-graph.jsp"),

   HISTORY_GRAPH("/jsp/plugin/events/events-history-graph.jsp");

   private String m_path;

   private JspFile(String path) {
      m_path = path;
   }

   public String getPath() {
      return m_path;
   }
}

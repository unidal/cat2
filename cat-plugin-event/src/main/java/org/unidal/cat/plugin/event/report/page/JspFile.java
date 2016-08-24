package org.unidal.cat.plugin.event.report.page;

public enum JspFile {
	REPORT("/jsp/plugin/event/event.jsp"),

	HOURLY_GRAPH("/jsp/plugin/event/event-hourly-graph.jsp"),

	HISTORY_GRAPH("/jsp/plugin/event/event-history-graph.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}

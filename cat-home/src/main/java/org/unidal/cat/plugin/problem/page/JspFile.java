package org.unidal.cat.plugin.problem.page;

public enum JspFile {

	HOURLY_REPORT("/jsp/report/problem/problemStatics.jsp"),

	DETAIL("/jsp/report/problem/problemDetail.jsp"),

	GROUP("/jsp/report/problem/problemGroup.jsp"),

	HOUR_GRAPH("/jsp/report/problem/problemHourlyGraphs.jsp"),

	HISTORY("/jsp/report/problem/problemHistoryReport.jsp"),

	HISTORY_GRAPH("/jsp/report/problem/problemHistoryGraphs.jsp"),

	MOBILE("/jsp/report/problem/problem_mobile.jsp"),

	THREAD("/jsp/report/problem/problemThread.jsp");

	private String m_path;

	JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}

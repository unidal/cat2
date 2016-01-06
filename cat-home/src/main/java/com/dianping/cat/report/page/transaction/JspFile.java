package com.dianping.cat.report.page.transaction;

public enum JspFile {
	HOURLY_REPORT("/jsp/report/transaction/transaction.jsp"),

	HOURLY_GRAPH("/jsp/report/transaction/transactionGraphs.jsp"),

	HISTORY_REPORT("/jsp/report/transaction/transactionHistoryReport.jsp"),

	HISTORY_GRAPH("/jsp/report/transaction/transactionHistoryGraphs.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}

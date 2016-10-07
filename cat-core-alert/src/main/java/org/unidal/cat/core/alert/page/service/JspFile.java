package org.unidal.cat.core.alert.page.service;

public enum JspFile {
	VIEW("/jsp/alert/service.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}

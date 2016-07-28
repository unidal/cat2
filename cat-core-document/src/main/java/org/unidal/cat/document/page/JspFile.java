package org.unidal.cat.document.page;

public enum JspFile {
	VIEW("/jsp/document/home.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}

package org.unidal.cat.core.message.page.home;

public enum JspFile {
	VIEW("/jsp/message/message.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}

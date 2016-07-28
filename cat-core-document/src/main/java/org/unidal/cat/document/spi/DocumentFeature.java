package org.unidal.cat.document.spi;

import java.util.Set;

public class DocumentFeature {
	private String m_id;

	private String m_title;

	private String m_url;

	private Set<String> m_afterFeatures;

	private Set<String> m_beforeFeatures;

	public DocumentFeature(String id, String title, String url) {
		m_id = id;
		m_title = title;
		m_url = url;
	}

	public String getId() {
		return m_id;
	}

	public String getTitle() {
		return m_title;
	}

	public String getUrl() {
		return m_url;
	}

	public Set<String> getAfterFeatures() {
		return m_afterFeatures;
	}

	public Set<String> getBeforeFeatures() {
		return m_beforeFeatures;
	}
}

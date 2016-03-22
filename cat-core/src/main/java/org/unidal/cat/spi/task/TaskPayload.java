package org.unidal.cat.spi.task;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.unidal.cat.spi.ReportPeriod;

public class TaskPayload {
	private String m_subject;

	private String m_key;

	private Map<String, String> m_properties = new LinkedHashMap<String, String>();

	public TaskPayload(String subject, String key) {
		m_subject = subject;
		m_key = key;
	}

	public TaskPayload(String subject, ReportPeriod period, Date startTime) {
		m_subject = subject;
		m_key = period.format(startTime);
	}

	public String getSubject() {
		return m_subject;
	}

	public String getKey() {
		return m_key;
	}

	public TaskPayload property(String key, Object value) {
		m_properties.put(key, String.valueOf(value));
		return this;
	}

	@Override
	public String toString() {
		return String.format("TaskPayload[%s,%s]", m_subject, m_key, m_properties);
	}
}

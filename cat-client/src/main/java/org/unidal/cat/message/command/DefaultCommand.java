package org.unidal.cat.message.command;

import java.util.HashMap;
import java.util.Map;

public class DefaultCommand implements Command {
	private String m_name;

	private Map<String, String> m_arguments = new HashMap<String, String>();

	private Map<String, String> m_headers = new HashMap<String, String>();

	private long m_timestamp;

	public DefaultCommand(String name, long timestamp) {
		m_name = name;
		m_timestamp = timestamp;
	}

	@Override
	public Map<String, String> getArguments() {
		return m_arguments;
	}

	@Override
	public Map<String, String> getHeaders() {
		return m_headers;
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public long getTimestamp() {
		return m_timestamp;
	}

	@Override
	public String toString() {
		return String.format("%s[name=%s, args=%s, timestamp=%s, headers=%s]", getClass().getSimpleName(), m_name,
		      m_arguments, m_timestamp, m_headers);
	}
}

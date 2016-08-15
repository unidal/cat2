package org.unidal.cat.spi.remote;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.Cat;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportFilter;

import com.dianping.cat.message.Transaction;

public class DefaultRemoteContext implements RemoteContext {
	private String m_name;

	private String m_domain;

	private Date m_startTime;

	private ReportPeriod m_period;

	private Map<String, String> m_properties;

	private ReportFilter<? extends Report> m_filter;

	private ThreadLocal<Transaction> m_parent = new ThreadLocal<Transaction>();

	public DefaultRemoteContext(String name, String domain, Date startTime, ReportPeriod period,
	      ReportFilter<? extends Report> filter) {
		m_name = name;
		m_domain = domain;
		m_startTime = startTime;
		m_period = period;
		m_filter = filter;
	}

	public DefaultRemoteContext(String name, String domain, long startTime, ReportPeriod period,
	      ReportFilter<? extends Report> filter) {
		m_name = name;
		m_domain = domain;
		m_startTime = new Date(startTime);
		m_period = period;
		m_filter = filter;
	}

	@Override
	public String buildURL(String serverUriPrefix) {
		StringBuilder sb = new StringBuilder(256);

		sb.append(serverUriPrefix);

		if (!serverUriPrefix.endsWith("/")) {
			sb.append('/');
		}

		sb.append(m_name);
		sb.append('/').append(m_domain);
		sb.append('/').append(m_period.getName());
		sb.append('/').append(m_period.format(m_startTime));

		if (m_filter != null) {
			sb.append('/').append(m_filter.getId());
		}

		if (m_properties != null && m_properties.size() > 0) {
			boolean first = true;

			sb.append('?');

			try {
				for (Map.Entry<String, String> e : m_properties.entrySet()) {
					String key = URLEncoder.encode(e.getKey(), "UTF-8");
					String value = URLEncoder.encode(e.getValue(), "UTF-8");

					if (first) {
						first = false;
					} else {
						sb.append('&');
					}

					sb.append(key).append('=').append(value);
				}
			} catch (UnsupportedEncodingException e) {
				Cat.logError(e);
			}
		}

		return sb.toString();
	}

	@Override
	public void destroy() {
		m_parent.remove();
		m_properties.clear();
	}

	@Override
	public String getDomain() {
		return m_domain;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Report> ReportFilter<T> getFilter() {
		return (ReportFilter<T>) m_filter;
	}

	@Override
	public int getIntProperty(String property, int defaultValue) {
		String value = getProperty(property, null);

		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {
				// ignore it
			}
		}

		return defaultValue;
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public Transaction getParentTransaction() {
		return m_parent.get();
	}

	@Override
	public ReportPeriod getPeriod() {
		return m_period;
	}

	@Override
	public Map<String, String> getProperties() {
		if (m_properties == null) {
			return Collections.emptyMap();
		} else {
			return m_properties;
		}
	}

	@Override
	public String getProperty(String property, String defaultValue) {
		if (m_properties == null) {
			return defaultValue;
		} else {
			String value = m_properties.get(property);

			if (value == null) {
				return defaultValue;
			} else {
				return value;
			}
		}
	}

	@Override
	public Date getStartTime() {
		return m_startTime;
	}

	@Override
	public void setParentTransaction(Transaction parent) {
		m_parent.set(parent);
	}

	@Override
	public RemoteContext setProperty(String property, String newValue) {
		if (newValue == null) {
			if (m_properties != null) {
				m_properties.remove(property);
			}
		} else {
			if (m_properties == null) {
				m_properties = new LinkedHashMap<String, String>();
			}

			m_properties.put(property, newValue);
		}

		return this;
	}

	@Override
	public String toString() {
		return buildURL("");
	}
}

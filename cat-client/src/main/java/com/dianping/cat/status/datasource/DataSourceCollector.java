package com.dianping.cat.status.datasource;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.dianping.cat.status.AbstractCollector;

public abstract class DataSourceCollector extends AbstractCollector {
	protected Map<String, Object> m_lastValueMap = new HashMap<String, Object>();

	protected MBeanServer m_mbeanServer = ManagementFactory.getPlatformMBeanServer();
	
	protected DatabaseParser m_databaseParser = new DatabaseParser();

	protected static final char SPLIT = '.';

	protected static final Integer ERROR_INT = -1;

	protected static final Long ERROR_LONG = -1l;

	protected static final String ERROR_ATTRIBUTE = "unknown";

	private Integer diffLast(String key, Integer value) {
		Object lastValue = m_lastValueMap.get(key);

		if (lastValue != null) {
			m_lastValueMap.put(key, value);
			return value - (Integer) lastValue;
		} else {
			m_lastValueMap.put(key, value);
			return value;
		}
	}

	private Long diffLast(String key, Long value) {
		Object lastValue = m_lastValueMap.get(key);
		if (lastValue != null) {
			m_lastValueMap.put(key, value);
			return value - (Long) lastValue;
		} else {
			m_lastValueMap.put(key, value);
			return value;
		}
	}

	@Override
	public String getDescription() {
		return "datasource.c3p0";
	}

	@Override
	public String getId() {
		return "datasource.c3p0";
	}

	protected Integer getIntegerAttribute(ObjectName objectName, String attribute, Boolean isDiff) {
		try {
			Integer value = (Integer) m_mbeanServer.getAttribute(objectName, attribute);
			if (isDiff) {
				return diffLast(objectName.getCanonicalName() + attribute, value);
			} else {
				return value;
			}
		} catch (Exception e) {
			return ERROR_INT;
		}
	}

	protected Long getLongAttribute(ObjectName objectName, String attribute, Boolean isDiff) {
		try {
			Long value = (Long) m_mbeanServer.getAttribute(objectName, attribute);
			if (isDiff) {
				return diffLast(objectName.getCanonicalName() + attribute, value);
			} else {
				return value;
			}
		} catch (Exception e) {
			return ERROR_LONG;
		}
	}

	protected String getStringAttribute(ObjectName objectName, String attribute) {
		try {
			return (String) m_mbeanServer.getAttribute(objectName, attribute);
		} catch (Exception e) {
			return ERROR_ATTRIBUTE;
		}
	}
	
	protected String getConnction(Map<String, Integer> datasources, String key) {
		Integer index = datasources.get(key);

		if (index == null) {
			datasources.put(key, 0);

			return key;
		} else {
			index++;

			datasources.put(key, index);
			return key + '[' + index + ']';
		}
	}

	protected Boolean isRandomName(String name) {
		return name != null && name.length() > 30;
	}

}

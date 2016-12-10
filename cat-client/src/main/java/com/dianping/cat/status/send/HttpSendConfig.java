package com.dianping.cat.status.send;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.dianping.cat.configuration.NetworkInterfaceManager;

public class HttpSendConfig {
	public static final String DFT_CONFIG_FILE_NAME = "jmonitor.properties";

	public static final String MULTI_PROPS_SEPERATOR = ",";// 分隔符

	private String m_endpoint;

	private String m_tag;

	private int m_step;

	private String m_url;

	private int m_maxItemHold;

	private boolean m_disabled;

	public static synchronized HttpSendConfig loadDefaultConfig() {
		String filename = DFT_CONFIG_FILE_NAME;
		Properties props = new Properties();
		InputStream is = HttpSendConfig.class.getClassLoader().getResourceAsStream(filename);

		if (is == null) {
			is = HttpSendConfig.class.getClassLoader().getResourceAsStream("http-config-dft.properties");
		}
		try {
			props.load(is);
		} catch (IOException e) {
			throw new RuntimeException("Http config failed to read " + filename, e);
		}
		HttpSendConfig config = new HttpSendConfig(props);
		return config;
	}

	private HttpSendConfig(Properties props) {
		m_disabled = Boolean.parseBoolean(props.getProperty("disabled", "false"));

		if (m_disabled) {
			return;
		}

		m_endpoint = props.getProperty("falcon.endpoint", getLocalHostName());
		m_tag = props.getProperty("http.agent.tag", "");
		m_step = Integer.parseInt(props.getProperty("http.agent.step", "60"));
		m_url = props.getProperty("http.agent.url", "http://127.0.0.1:1988/v1/push");
		m_maxItemHold = Integer.parseInt(props.getProperty("max-item-hold", "8192"));
	}

	public String getEndpoint() {
		return m_endpoint;
	}

	private String getLocalHostName() {
		String hostname = NetworkInterfaceManager.INSTANCE.getLocalHostName();

		if (hostname == null) {
			hostname = "unkown";
		} else if (hostname.contains(".sankuai.com")) {
			hostname = hostname.substring(0, hostname.indexOf("."));
		}

		return hostname;
	}

	public int getMaxItemHold() {
		return m_maxItemHold;
	}

	public int getStep() {
		return m_step;
	}

	public String getTag() {
		return m_tag;
	}

	public String getUrl() {
		return m_url;
	}

	public boolean isDisabled() {
		return m_disabled;
	}

	public void setDisabled(boolean disabled) {
		m_disabled = disabled;
	}

	public void setEndpoint(String endpoint) {
		m_endpoint = endpoint;
	}

	public void setMaxItemHold(int maxItemHold) {
		m_maxItemHold = maxItemHold;
	}

	public void setStep(int step) {
		m_step = step;
	}

	public void setTag(String tag) {
		m_tag = tag;
	}

	public void setUrl(String url) {
		m_url = url;
	}

}

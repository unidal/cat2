package com.dianping.cat.configuration;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultSaxParser;
import com.dianping.cat.configuration.property.entity.PropertyConfig;
import com.dianping.cat.message.spi.MessageTree;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.helper.Files;
import org.unidal.helper.Urls;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Named(type = ClientConfigManager.class)
public class DefaultClientConfigManager implements LogEnabled, ClientConfigManager, Initializable {

	private static final String PROPERTIES_FILE = EnvironmentHelper.PROPERTIES_FILE;

	private ClientConfig m_config;

	private volatile double m_sample = 1d;

	private volatile boolean m_block = false;

	private String m_routers;

	private AtomicTreeParser m_atomicTreeParser = new AtomicTreeParser();

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getDomain() {
		if (m_config != null) {
			return m_config.getDomain();
		} else {
			return Cat.UNKNOWN;
		}
	}

	@Override
	public int getMaxMessageLength() {
		if (m_config == null) {
			return 5000;
		} else {
			return m_config.getMaxMessageSize();
		}
	}

	@Override
	public String getRouters() {
		if (m_routers == null) {
			refreshConfig();
		}
		return m_routers;
	}

	public double getSampleRatio() {
		return m_sample;
	}

	private String getServerConfigUrl() {
		if (m_config == null) {
			return null;
		} else {
			List<Server> servers = m_config.getServers();
			int size = servers.size();
			int index = (int) (size * Math.random());

			if (index >= 0 && index < size) {
				Server server = servers.get(index);

				Integer httpPort = server.getHttpPort();

				if (httpPort == null || httpPort == 0) {
					httpPort = 8080;
				}
				return String.format("http://%s:%d/cat/s/router?domain=%s&ip=%s&op=xml&env=%s", server.getIp().trim(),
				      httpPort, getDomain(), NetworkInterfaceManager.INSTANCE.getLocalHostAddress(),
				      EnvironmentHelper.ENVIRONMENT);
			}
		}
		return null;
	}

	@Override
	public List<Server> getServers() {
		if (m_config == null) {
			return Collections.emptyList();
		} else {
			return m_config.getServers();
		}
	}

	@Override
	public int getTaggedTransactionCacheSize() {
		return 1024;
	}

	@Override
	public void initialize() {
		String config = System.getProperty("cat-client-config");

		if (StringUtils.isNotEmpty(config)) {
			try {
				ClientConfig clientConfig = DefaultSaxParser.parse(config);

				initialize(clientConfig);
			} catch (Exception e) {
				m_logger.error("error in client config " + config, e);
				initializeWithDefault();
			}
		} else {
			initializeWithDefault();
		}
	}

	private void initializeWithDefault() {
		String clientXml = Cat.getCatHome() + "client.xml";
		File configFile = new File(clientXml);

		m_logger.info("client xml path " + clientXml);

		ClientConfig globalConfig = null;
		String xml = null;

		if (configFile != null && configFile.exists()) {
			try {
				xml = Files.forIO().readFrom(configFile.getCanonicalFile(), "utf-8");

				globalConfig = DefaultSaxParser.parse(xml);
				m_logger.info("local client config file found." + xml);
			} catch (Exception e) {
				m_logger.error("error when parse xml " + xml, e);
				globalConfig = new ClientConfig();
			}
		} else {
			try {
				xml = EnvironmentHelper.fetchClientConfig();

				globalConfig = DefaultSaxParser.parse(xml);
				m_logger.info("fetch remote config file." + xml);
			} catch (Exception e) {
				m_logger.error("error when parse remote server xml " + xml, e);
				globalConfig = new ClientConfig();
			}
		}
		String appName = String.valueOf(loadProjectName());
		globalConfig.setDomain(appName);

		m_config = globalConfig;
		m_logger.info("setup cat with default config:" + m_config);
	}

	@Override
	public void initialize(ClientConfig config) {
		try {
			if (config != null) {
				m_config = config;
				m_logger.info("setup cat with config:" + config);
			}
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			m_config = new ClientConfig();
		}
	}

	@Override
	public boolean isAtomicMessage(MessageTree tree) {
		return tree.canDiscard() && m_atomicTreeParser.isAtomicMessage(tree);
	}

	public boolean isBlock() {
		return m_block;
	}

	@Override
	public boolean isCatEnabled() {
		if (m_config == null) {
			return false;
		} else {
			return m_config.isEnabled() && !getDomain().equals(Cat.UNKNOWN);
		}
	}

	@Override
	public boolean isDumpLocked() {
		if (m_config == null) {
			return false;
		} else {
			return m_config.isDumpLocked();
		}
	}

	private String loadProjectName() {
		InputStream in = null;
		try {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE);

			if (in == null) {
				in = Cat.class.getResourceAsStream(PROPERTIES_FILE);
			}
			if (in != null) {
				Properties prop = new Properties();

				prop.load(in);

				String appName = prop.getProperty("app.name");
				if (appName != null) {
					m_logger.info(String.format("Find domain name %s from app.properties.", appName));
					return appName;
				} else {
					m_logger.info("Can't find app.name from app.properties.");
				}
			} else {
				m_logger.info(String.format("Can't find app.properties in %s", PROPERTIES_FILE));
			}
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
		return Cat.UNKNOWN;
	}

	public void setSample(double sample) {
		m_sample = sample;
	}

	public void refreshConfig() {
		String url = getServerConfigUrl();

		try {
			InputStream inputstream = Urls.forIO().readTimeout(2000).connectTimeout(1000).openStream(url);
			String content = Files.forIO().readFrom(inputstream, "utf-8");
			PropertyConfig routerConfig = com.dianping.cat.configuration.property.transform.DefaultSaxParser.parse(content
			      .trim());
			m_routers = routerConfig.findProperty("routers").getValue();
			m_sample = Double.parseDouble(routerConfig.findProperty("sample").getValue());

			if (m_sample < 0) {
				m_sample = 1d;
			}

			m_block = Boolean.parseBoolean(routerConfig.findProperty("block").getValue());
			String startTypes = routerConfig.findProperty("startTransactionTypes").getValue();
			String matchTypes = routerConfig.findProperty("matchTransactionTypes").getValue();

			m_atomicTreeParser.init(startTypes, matchTypes);
		} catch (Exception e) {
			m_logger.warn("error when connect cat server config url " + url);
		}
	}

}

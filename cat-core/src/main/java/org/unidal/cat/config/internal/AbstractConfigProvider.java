package org.unidal.cat.config.internal;

import com.dianping.cat.Cat;
import org.codehaus.plexus.logging.LogEnabled;
import org.unidal.cat.config.ConfigManager;
import org.unidal.cat.config.ConfigProvider;
import org.unidal.lookup.annotation.Inject;
import org.xml.sax.SAXException;

import java.io.IOException;

public abstract class AbstractConfigProvider<T> implements ConfigProvider<T> {
    @Inject
    private ConfigManager m_manager;

    protected T getConfig() {
        String configString = m_manager.getConfigString(getConfigKey());
        T config = null;
        try {
            config = parse(configString);
        } catch (Throwable e) {
            // m_logger
            Cat.logError(e);
        }

        return config;
    }

    protected abstract String getConfigKey();

    protected abstract T parse(String configString) throws IOException, SAXException;
}

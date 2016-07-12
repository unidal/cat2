package org.unidal.cat.config.internal;

import java.io.IOException;

import org.unidal.cat.config.ConfigManager;
import org.unidal.cat.config.ConfigProvider;
import org.unidal.lookup.annotation.Inject;
import org.xml.sax.SAXException;

public abstract class AbstractConfigProvider<T> implements ConfigProvider<T> {
    @Inject
    private ConfigManager m_manager;

    protected T getConfig() {
        String configString = m_manager.getConfigString(getConfigKey());
        T config = null;
        try {
            config = parse(configString);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return config;
    }

    protected abstract String getConfigKey();

    protected abstract T parse(String configString) throws IOException, SAXException;
}

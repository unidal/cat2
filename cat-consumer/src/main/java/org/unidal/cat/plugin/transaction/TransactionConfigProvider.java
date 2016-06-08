package org.unidal.cat.plugin.transaction;

import org.apache.commons.lang.StringUtils;
import org.unidal.cat.config.ConfigProvider;
import org.unidal.cat.config.internal.AbstractConfigProvider;
import org.unidal.cat.plugin.transaction.config.entity.All;
import org.unidal.cat.plugin.transaction.config.entity.TransactionConfig;
import org.unidal.cat.plugin.transaction.config.transform.DefaultSaxParser;
import org.unidal.lookup.annotation.Named;
import org.xml.sax.SAXException;

import java.io.IOException;

@Named(type=TransactionConfigProvider.class)
public class TransactionConfigProvider extends AbstractConfigProvider<TransactionConfig> {
    @Override
    protected String getConfigKey() {
        return TransactionConstants.NAME;
    }

    @Override
    protected TransactionConfig parse(String configString) throws IOException, SAXException {
        return configString==null ? null : DefaultSaxParser.parse(configString);
    }

    public boolean shouldMakeAllReport(String type) {
        TransactionConfig config = getConfig();
        if (null == config) return false;
        for (All all : config.getAlls()) {
            String t = all.getType();
            String n = all.getName();
            if (t.equals(type) && (StringUtils.isBlank(n) || "*".equals(n))) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldMakeAllReport(String type, String name) {
        TransactionConfig config = getConfig();
        if (null == config) return false;
        for (All all : config.getAlls()) {
            String t = all.getType();
            String n = all.getName();
            if (t.equals(type) && n.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public int getMaxNameEntries(String domain, String type) {
        return 0;
    }

    public boolean isIgnored(String domain, String type) {
        return false;
    }
}

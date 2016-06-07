package org.unidal.cat.plugin.transaction;

import org.unidal.cat.config.ConfigProvider;
import org.unidal.cat.config.internal.AbstractConfigProvider;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.lookup.annotation.Named;
import org.xml.sax.SAXException;

import java.io.IOException;

@Named(type= ConfigProvider.class, value= TransactionConstants.NAME)
public class TransactionConfigProvider extends AbstractConfigProvider<Object> {
    @Override
    protected String getConfigKey() {
        return TransactionConstants.NAME;
    }

    @Override
    protected Object parse(String configString) throws IOException, SAXException {
        return null;
    }

    public int getMaxNameEntries(String domain, String type) {
        return 0;
    }

    public boolean isIgnored(String domain, String type) {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }
}

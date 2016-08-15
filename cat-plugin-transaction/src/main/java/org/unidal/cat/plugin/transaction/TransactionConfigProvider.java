package org.unidal.cat.plugin.transaction;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.unidal.cat.config.internal.AbstractConfigProvider;
import org.unidal.cat.plugin.transaction.config.entity.AllModel;
import org.unidal.cat.plugin.transaction.config.entity.TransactionConfigModel;
import org.unidal.cat.plugin.transaction.config.transform.DefaultSaxParser;
import org.unidal.lookup.annotation.Named;
import org.xml.sax.SAXException;

@Named(type = TransactionConfigProvider.class)
public class TransactionConfigProvider extends AbstractConfigProvider<TransactionConfigModel> {
   @Override
   protected String getConfigKey() {
      return TransactionConstants.NAME;
   }

   public int getMaxNameEntries(String domain, String type) {
      return 0;
   }

   public boolean isIgnored(String domain, String type) {
      return false;
   }

   @Override
   protected TransactionConfigModel parse(String configString) throws IOException, SAXException {
      return configString == null ? null : DefaultSaxParser.parse(configString);
   }

   public boolean shouldMakeAllReport(String type) {
      TransactionConfigModel config = getConfig();
      if (null == config) return false;
      for (AllModel all : config.getAlls()) {
         String t = all.getType();
         String n = all.getName();
         if (t.equals(type) && (StringUtils.isBlank(n) || "*".equals(n))) {
            return true;
         }
      }
      return false;
   }

   public boolean shouldMakeAllReport(String type, String name) {
      TransactionConfigModel config = getConfig();
      if (null == config) return false;
      for (AllModel all : config.getAlls()) {
         String t = all.getType();
         String n = all.getName();
         if (t.equals(type) && n.equals(name)) {
            return true;
         }
      }
      return false;
   }
}

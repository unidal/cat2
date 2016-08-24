package org.unidal.cat.plugin.event;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.unidal.cat.config.internal.AbstractConfigProvider;
import org.unidal.cat.plugin.event.config.entity.AllModel;
import org.unidal.cat.plugin.event.config.entity.EventConfigModel;
import org.unidal.cat.plugin.event.config.transform.DefaultSaxParser;
import org.unidal.lookup.annotation.Named;
import org.xml.sax.SAXException;

@Named(type = EventConfigProvider.class)
public class EventConfigProvider extends AbstractConfigProvider<EventConfigModel> {
   @Override
   protected String getConfigKey() {
      return EventConstants.NAME;
   }

   public int getMaxNameEntries(String domain, String type) {
      return 0;
   }

   public boolean isIgnored(String domain, String type) {
      return false;
   }

   @Override
   protected EventConfigModel parse(String configString) throws IOException, SAXException {
      return configString == null ? null : DefaultSaxParser.parse(configString);
   }

   public boolean shouldMakeAllReport(String type) {
      EventConfigModel config = getConfig();
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
      EventConfigModel config = getConfig();
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

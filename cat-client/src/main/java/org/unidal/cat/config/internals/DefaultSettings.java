package org.unidal.cat.config.internals;

import java.io.File;
import java.io.IOException;

import org.unidal.lookup.annotation.Named;

@Named(type = Settings.class)
public class DefaultSettings implements Settings {
   private String m_home;

   @Override
   public String getCatHome() {
      if (m_home == null) {
         String home = System.getProperty("cat.home", null);

         if (home == null) {
            home = System.getenv("CAT_HOME");
         }

         if (home != null) {
            m_home = home;
         } else {
            m_home = "/data/appdatas/cat";
         }
      }

      return m_home;
   }

   @Override
   public File getClientXmlFile() {
      File file = new File(getCatHome(), "client.xml");

      try {
         return file.getCanonicalFile();
      } catch (IOException e) {
         return file;
      }
   }

   @Override
   public int getDefaultServerHttpPort() {
      return 2281;
   }

   @Override
   public String getRemoteConfigUrlPattern() {
      return "http://%s:%s/cat/config/client?domain=%s";
   }
}

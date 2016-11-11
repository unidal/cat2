package org.unidal.cat.core.alert.page;

import org.unidal.web.mvc.Page;
import org.unidal.web.mvc.annotation.ModuleMeta;

public enum AlertPage implements Page {

   HOME("home", "home", "Home", "Home", true),

   SERVICE("service", "service", "Service", "Service", true),

   UPDATE("update", "update", "Update", "Update", true);

   private String m_name;

   private String m_path;

   private String m_title;

   private String m_description;

   private boolean m_standalone;

   private AlertPage(String name, String path, String title, String description, boolean standalone) {
      m_name = name;
      m_path = path;
      m_title = title;
      m_description = description;
      m_standalone = standalone;
   }

   public static AlertPage getByName(String name, AlertPage defaultPage) {
      for (AlertPage action : AlertPage.values()) {
         if (action.getName().equals(name)) {
            return action;
         }
      }

      return defaultPage;
   }

   public String getDescription() {
      return m_description;
   }

   public String getModuleName() {
      ModuleMeta meta = AlertModule.class.getAnnotation(ModuleMeta.class);

      if (meta != null) {
         return meta.name();
      } else {
         return null;
      }
   }

   @Override
   public String getName() {
      return m_name;
   }

   @Override
   public String getPath() {
      return m_path;
   }

   public String getTitle() {
      return m_title;
   }

   public boolean isStandalone() {
      return m_standalone;
   }

   public AlertPage[] getValues() {
      return AlertPage.values();
   }
}

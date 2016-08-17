package org.unidal.cat.plugin.transactions.report;

import org.unidal.web.mvc.Page;
import org.unidal.web.mvc.annotation.ModuleMeta;

public enum ReportPage implements Page {

   TRANSACTIONS("transactions", "ts", "Transactions", "Transactions");

   private String m_name;

   private String m_path;

   private String m_title;

   private String m_description;

   private ReportPage(String name, String path, String title, String description) {
      m_name = name;
      m_path = path;
      m_title = title;
      m_description = description;
   }

   public static ReportPage getByName(String name, ReportPage defaultPage) {
      for (ReportPage action : ReportPage.values()) {
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
      ModuleMeta meta = ReportModule.class.getAnnotation(ModuleMeta.class);

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

   public ReportPage[] getValues() {
      return ReportPage.values();
   }
}

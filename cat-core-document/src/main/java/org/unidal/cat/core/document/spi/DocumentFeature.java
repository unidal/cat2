package org.unidal.cat.core.document.spi;

import java.util.Set;

public class DocumentFeature {
   private String m_id;

   private String m_title;

   private String m_url;

   private Set<String> m_afterFeatures;

   private Set<String> m_beforeFeatures;

   public DocumentFeature(String id, String title, String url) {
      m_id = id;
      m_title = title;
      m_url = url;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof DocumentFeature) {
         return m_id.equals(((DocumentFeature) obj).m_id);
      }

      return false;
   }

   public Set<String> getAfterFeatures() {
      return m_afterFeatures;
   }

   public Set<String> getBeforeFeatures() {
      return m_beforeFeatures;
   }

   public String getId() {
      return m_id;
   }

   public String getTitle() {
      return m_title;
   }

   public String getUrl() {
      return m_url;
   }

   @Override
   public int hashCode() {
      return m_id.hashCode();
   }
}

package org.unidal.cat.core.alert.message;

public class AlertRecipient {
   private String m_type;

   private String m_id;

   public AlertRecipient(String type, String id) {
      m_type = type;
      m_id = id;
   }

   public String getType() {
      return m_type;
   }

   public String getId() {
      return m_id;
   }
}

package org.unidal.cat.core.alert;

import java.util.Date;

public abstract class AbstractAlertEvent implements AlertEvent {
   private String m_eventName;

   private Date m_timestamp;

   protected AbstractAlertEvent(String eventName) {
      m_eventName = eventName;
      m_timestamp = new Date();
   }

   public String getEventName() {
      return m_eventName;
   }

   public Date getTimestamp() {
      return m_timestamp;
   }

   public AbstractAlertEvent setTimestamp(Date timestamp) {
      m_timestamp = timestamp;
      return this;
   }
}

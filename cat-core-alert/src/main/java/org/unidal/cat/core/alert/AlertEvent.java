package org.unidal.cat.core.alert;

import java.util.Date;

public interface AlertEvent {
   /**
    * Name of alert event.
    * <p>
    * It could be <code>project domain</code>, or <code>database name</code>, or <code>cache cluster</code>, or
    * <code>message cluster</code> etc.
    * <p>
    * 
    * @return event name
    */
   public String getEventName();

   /**
    * When this alert event happens.
    * 
    * @return event timestamp
    */
   public Date getTimestamp();
}

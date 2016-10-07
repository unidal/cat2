package org.unidal.cat.core.alert.engine;

public interface AlertListener {
   public String getStatement();

   public void onEvent(AlertContext ctx);

   public Class<?> getEventClass();

   public String getEventName();
}

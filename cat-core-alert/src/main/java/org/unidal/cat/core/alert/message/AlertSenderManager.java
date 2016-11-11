package org.unidal.cat.core.alert.message;

public interface AlertSenderManager {
   public AlertSender getSender(String type, String action);
}

package org.unidal.cat.core.alert.message;

public interface AlertSender {
   public String getType();

   public void send(AlertMessage message, AlertRecipient recipient);
}

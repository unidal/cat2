package org.unidal.cat.core.alert.message;

import org.unidal.cat.core.alert.AlertConstants;
import org.unidal.lookup.annotation.Named;

@Named(type = AlertSender.class, value = AlertConstants.TYPE_EMAIL)
public class EmailAlertSender implements AlertSender {
   @Override
   public String getType() {
      return AlertConstants.TYPE_EMAIL;
   }

   @Override
   public void send(AlertMessage message, AlertRecipient recipient) {
      // TODO
      System.out.println(message);
   }
}

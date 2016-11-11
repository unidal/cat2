package org.unidal.cat.core.alert.message;

import org.unidal.cat.core.alert.AlertConstants;
import org.unidal.lookup.annotation.Named;

@Named(type = AlertSender.class, value = AlertConstants.EMAIL)
public class EmailAlertSender implements AlertSender {
   @Override
   public String getType() {
      return AlertConstants.EMAIL;
   }

   @Override
   public void send(AlertMessage message, AlertRecipient recipient) {
      // TODO
      System.out.println(message);
   }
}

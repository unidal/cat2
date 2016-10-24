package org.unidal.cat.core.alert.metric.handler;

import org.unidal.cat.core.alert.message.AlertMessage;
import org.unidal.lookup.annotation.Named;

@Named(type = AlertMessageSink.class)
public class DefaultAlertMessageSink implements AlertMessageSink {

   @Override
   public void add(AlertMessage message) {
      System.out.println(message);
      System.out.println();
   }
}

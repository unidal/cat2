package org.unidal.cat.core.alert.metric.handler;

import org.unidal.cat.core.alert.message.AlertMessage;

public interface AlertMessageSink {
   public void add(AlertMessage message);
}

package org.unidal.cat.core.alert.message;

import java.util.Map;

public interface AlertRecipientManager {
   public Map<String, AlertRecipient> getRecipients(AlertMessage message);
}

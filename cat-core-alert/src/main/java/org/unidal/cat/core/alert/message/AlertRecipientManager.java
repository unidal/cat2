package org.unidal.cat.core.alert.message;

import java.util.List;
import java.util.Map;

public interface AlertRecipientManager {
   public Map<String, List<AlertRecipient>> getRecipients(AlertMessage message);
}

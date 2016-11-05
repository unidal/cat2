package org.unidal.cat.core.alert.message;

import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = AlertSenderManager.class)
public class DefaultAlertSenderManager extends ContainerHolder implements AlertSenderManager, Initializable {
   private Map<String, AlertSender> m_senders;

   @Override
   public AlertSender getSender(String type, String action) {
      AlertSender sender = m_senders.get(type + ":" + action); // i.e. transaction:email, transaction:sms

      if (sender == null) {
         sender = m_senders.get(type);
      }

      if (sender == null) {
         throw new IllegalStateException(String.format("No AlertSender(%s:%s) registered!", type, action));
      } else {
         return sender;
      }
   }

   @Override
   public void initialize() throws InitializationException {
      m_senders = lookupMap(AlertSender.class);
   }
}

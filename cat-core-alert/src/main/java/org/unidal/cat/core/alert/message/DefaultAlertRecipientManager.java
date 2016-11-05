package org.unidal.cat.core.alert.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.cat.core.alert.rule.entity.AlertActionDef;
import org.unidal.cat.core.config.service.ContactorService;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.site.helper.Splitters;

@Named(type = AlertRecipientManager.class)
public class DefaultAlertRecipientManager implements AlertRecipientManager {
   @Inject
   private ContactorService m_service;

   @Override
   public Map<String, List<AlertRecipient>> getRecipients(AlertMessage message) {
      Map<String, List<AlertRecipient>> map = new HashMap<String, List<AlertRecipient>>();
      List<AlertActionDef> actions = message.getRule().getActions();

      for (AlertActionDef action : actions) {
         String type = action.getType();
         List<AlertRecipient> recipients = resolveRecipients(type, action.getRecipients());

         map.put(type, recipients);
      }

      return map;
   }

   private List<AlertRecipient> resolveRecipients(String type, String str) {
      List<AlertRecipient> recipients = new ArrayList<AlertRecipient>();
      List<String> ids = Splitters.by(',').noEmptyItem().trim().split(str);

      for (String id : ids) {
         String contactId = m_service.getContactor(type, id);

         if (contactId != null) {
            recipients.add(new AlertRecipient(type, id));
         }
      }

      return recipients;
   }
}

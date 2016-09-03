package org.unidal.cat.plugin.event;

import java.util.List;

import org.unidal.cat.plugin.event.model.entity.EventName;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.plugin.event.model.entity.EventType;
import org.unidal.cat.plugin.event.model.entity.Range;
import org.unidal.cat.spi.analysis.AbstractMessageAnalyzer;
import org.unidal.cat.spi.analysis.MessageAnalyzer;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@Named(type = MessageAnalyzer.class, value = EventConstants.NAME, instantiationStrategy = Named.PER_LOOKUP)
public class EventReportAnalyzer extends AbstractMessageAnalyzer<EventReport> {
   @Inject
   private EventConfigService m_configService;

   @Override
   public void process(MessageTree tree) {
      if (tree instanceof DefaultMessageTree) {
         List<Event> events = ((DefaultMessageTree) tree).getEvents();
         EventReport report = getLocalReport(tree.getDomain());

         for (Event event : events) {
            processEvent(report, tree, event);
         }
      }
   }

   private void processEvent(EventReport report, MessageTree tree, Event event) {
      if (m_configService.isEligible(tree.getDomain())) {
         String ip = tree.getIpAddress();
         EventType t = report.findOrCreateMachine(ip).findOrCreateType(event.getType());
         EventName n = t.findOrCreateName(event.getName());

         report.addIp(ip);
         processTypeAndName(event, t, n, tree.getMessageId());
      }
   }

   private void processTypeAndName(Event event, EventType type, EventName name, String messageId) {
      type.incTotalCount();
      name.incTotalCount();

      if (event.isSuccess()) {
         if (type.getSuccessMessageUrl() == null) {
            type.setSuccessMessageUrl(messageId);
         }

         if (name.getSuccessMessageUrl() == null) {
            name.setSuccessMessageUrl(messageId);
         }
      } else {
         type.incFailCount();
         name.incFailCount();

         if (type.getFailMessageUrl() == null) {
            type.setFailMessageUrl(messageId);
         }

         if (name.getFailMessageUrl() == null) {
            name.setFailMessageUrl(messageId);
         }
      }

      long current = event.getTimestamp() / 1000 / 60;
      int min = (int) (current % 60);

      Range range = name.findOrCreateRange(min);

      range.incCount();
   }
}

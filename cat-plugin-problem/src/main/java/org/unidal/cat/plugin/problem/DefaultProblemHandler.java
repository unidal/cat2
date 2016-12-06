package org.unidal.cat.plugin.problem;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Named(type = ProblemHandler.class, value = DefaultProblemHandler.ID)
public class DefaultProblemHandler extends AbstractProblemHandler {
   public static final String ID = "default";

   @Inject
   private ServerConfigManager m_configManager;

   private Set<String> m_errorTypes;

   @Override
   public void handle(Machine machine, MessageTree tree) {
      Message message = tree.getMessage();

      // ctrip yj.huang Utilize the transaction/event/heartbeat index to improve performance.
      if (tree instanceof DefaultMessageTree) {
         for (Transaction t : ((DefaultMessageTree) tree).getTransactions()) {
            processTransaction(machine, t, tree, false);
         }
         for (Event e : ((DefaultMessageTree) tree).getEvents()) {
            processEvent(machine, e, tree);
         }
         for (Heartbeat h : ((DefaultMessageTree) tree).getHeartbeats()) {
            processHeartbeat(machine, h, tree);
         }
      } else if (message instanceof Transaction) {
         processTransaction(machine, (Transaction) message, tree, true);
      } else if (message instanceof Event) {
         processEvent(machine, (Event) message, tree);
      } else if (message instanceof Heartbeat) {
         processHeartbeat(machine, (Heartbeat) message, tree);
      }
   }

   private void processEvent(Machine machine, Event message, MessageTree tree) {
      if (!message.getStatus().equals(Message.SUCCESS) && m_errorTypes.contains(message.getType())) {
         String type = ProblemType.ERROR.getName();
         String status = message.getName();

         Entity entity = findOrCreateEntity(machine, type, status);
         updateEntity(tree, entity, 0);
      }
   }

   private void processHeartbeat(Machine machine, Heartbeat heartbeat, MessageTree tree) {
      String type = heartbeat.getType().toLowerCase();

      if ("heartbeat".equals(type)) {
         String status = heartbeat.getName();
         Entity entity = findOrCreateEntity(machine, type, status);

         updateEntity(tree, entity, 0);
      }
   }

   private void processTransaction(Machine machine, Transaction transaction, MessageTree tree, boolean recursive) {
      String transactionStatus = transaction.getStatus();

      if (!transactionStatus.equals(Transaction.SUCCESS)) {
         String type = transaction.getType();
         String name = transaction.getName();
         Entity entity = findOrCreateEntity(machine, type, name);

         updateEntity(tree, entity, 0);
      }

      List<Message> children = transaction.getChildren();

      if (recursive) {
         for (Message message : children) {
            if (message instanceof Transaction) {
               processTransaction(machine, (Transaction) message, tree, recursive);
            } else if (message instanceof Event) {
               processEvent(machine, (Event) message, tree);
            } else if (message instanceof Heartbeat) {
               processHeartbeat(machine, (Heartbeat) message, tree);
            }
         }
      }
   }

   public void setErrorType(String type) {
      m_errorTypes = new HashSet<String>(Splitters.by(',').noEmptyItem().split(type));
   }
}
package org.unidal.cat.plugin.transaction.alert;

import org.unidal.cat.core.alert.engine.AlertContext;
import org.unidal.cat.core.alert.engine.AlertEventSource;
import org.unidal.cat.core.alert.engine.AlertListener;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.lookup.annotation.Named;

@Named(type = AlertListener.class, value = TransactionConstants.NAME)
public class TransactionAlertListener implements AlertListener {
   @Override
   public String getStatement() {
      return "select domain,fromIp,type,name,total,fail," //
            + "sum(fail) as fails,sum(duration)/sum(total) as avg" //
            + " from " + getEventName() + ".win:length_batch(3)" //
            + " group by domain" //
            + " having sum(duration)/sum(total) > 100 or sum(fail) >= 1" //
            + " output every 1 seconds"; //
   }

   @Override
   public void onEvent(AlertContext ctx) {
      System.err.println("Transaction Alert: " + ctx.getRows());

      for (int i = 0; i < ctx.getRows(); i++) {
         System.err.println(ctx.getRow(i));
      }
   }

   @Override
   public Class<? extends AlertEventSource> getEventClass() {
      return TransactionEvent.class;
   }

   @Override
   public String getEventName() {
      return TransactionConstants.NAME;
   }
}

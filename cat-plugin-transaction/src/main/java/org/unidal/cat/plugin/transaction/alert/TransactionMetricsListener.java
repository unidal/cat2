package org.unidal.cat.plugin.transaction.alert;

import org.unidal.cat.core.alert.metric.AbstractMetricsListener;
import org.unidal.cat.core.alert.metric.MetricsListener;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.lookup.annotation.Named;

@Named(type = MetricsListener.class, value = TransactionConstants.NAME)
public class TransactionMetricsListener extends AbstractMetricsListener<TransactionMetrics> {
   protected String getSegmentId(TransactionMetrics m) {
      return m.getTypeName() + ":" + m.getDomain() + ":" + m.getType() + ":" + m.getName();
   }

   @Override
   public Class<TransactionMetrics> getType() {
      return TransactionMetrics.class;
   }
}

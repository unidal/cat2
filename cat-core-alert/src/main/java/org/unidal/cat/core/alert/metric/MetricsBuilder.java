package org.unidal.cat.core.alert.metric;

import org.unidal.cat.core.alert.model.entity.AlertEvent;

public interface MetricsBuilder {
   /**
    * Construct metrics and add them to the given event.
    * 
    * @param event
    *           to add metrics
    */
   public void build(AlertEvent event);

   public Class<? extends Metrics> getMetricsType();
}

package org.unidal.cat.core.alert.engine;

import org.unidal.cat.core.alert.model.entity.AlertMetric;

import com.espertech.esper.client.EPServiceProvider;

public interface AlertRegistry {
   public Object buildEvent(AlertMetric metric);

   public void register(EPServiceProvider esper, AlertListener listener);
}

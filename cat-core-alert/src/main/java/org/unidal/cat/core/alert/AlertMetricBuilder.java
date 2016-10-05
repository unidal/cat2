package org.unidal.cat.core.alert;

import org.unidal.cat.core.alert.model.entity.AlertEvent;

public interface AlertMetricBuilder {
   public void build(AlertEvent event);
}

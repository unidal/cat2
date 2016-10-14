package org.unidal.cat.core.alert.metric;

import org.unidal.cat.core.alert.model.entity.AlertMetric;

public interface Metrics {
   public String getTypeName();

   public String getTypeClass();

   public String getFromIp();

   public AlertMetric getAlertMetric();
}

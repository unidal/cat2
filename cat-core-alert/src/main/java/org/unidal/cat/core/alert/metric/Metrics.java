package org.unidal.cat.core.alert.metric;

import org.unidal.cat.core.alert.model.entity.AlertMetric;

public interface Metrics {
   public AlertMetric getAlertMetric();

   public String getFromIp();

   public String getTypeClass();

   public String getTypeName();
}

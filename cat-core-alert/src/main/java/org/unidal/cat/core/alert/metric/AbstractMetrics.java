package org.unidal.cat.core.alert.metric;

import org.unidal.cat.core.alert.model.entity.AlertMetric;

public abstract class AbstractMetrics implements Metrics {
   private AlertMetric m_metric;

   public AbstractMetrics(AlertMetric metric) {
      m_metric = metric;
   }

   @Override
   public AlertMetric getAlertMetric() {
      return m_metric;
   }

   @Override
   public String getFromIp() {
      return m_metric.getFromIp();
   }

   @Override
   public String getTypeClass() {
      return m_metric.getTypeClass();
   }

   @Override
   public String getTypeName() {
      return m_metric.getTypeName();
   }

   @Override
   public String toString() {
      return m_metric != null ? m_metric.toString() : "AlertMetric[]";
   }
}

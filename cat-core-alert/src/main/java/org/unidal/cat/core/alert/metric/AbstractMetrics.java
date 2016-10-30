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

   protected double getDouble(String property, double defaultValue) {
      try {
         return Double.parseDouble(m_metric.get(property));
      } catch (Exception e) {
         return defaultValue;
      }
   }

   @Override
   public String getFromIp() {
      return m_metric.getFromIp();
   }

   protected int getInt(String property, int defaultValue) {
      try {
         return Integer.parseInt(m_metric.get(property));
      } catch (Exception e) {
         return defaultValue;
      }
   }

   protected long getLong(String property, long defaultValue) {
      try {
         return Long.parseLong(m_metric.get(property));
      } catch (Exception e) {
         return defaultValue;
      }
   }

   protected String getString(String property) {
      return getString(property, null);
   }

   protected String getString(String property, String defaultValue) {
      try {
         return m_metric.get(property);
      } catch (Exception e) {
         return defaultValue;
      }
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

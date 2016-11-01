package org.unidal.cat.plugin.transaction.alert;

import org.unidal.cat.core.alert.metric.Metrics;

public enum TransactionField {
   DOMAIN,

   TYPE,

   NAME,

   HITS,

   FAILS,

   DURATION;

   private String m_name;

   private TransactionField() {
      m_name = name().toLowerCase();
   }

   public static TransactionField getByName(String name) {
      for (TransactionField field : values()) {
         if (field.getName().equals(name)) {
            return field;
         }
      }

      throw new IllegalStateException("Unknown " + TransactionField.class.getSimpleName() + ": " + name);
   }

   public double getDouble(Metrics metrics) {
      return metrics.getAlertMetric().getDouble(m_name, 0);
   }

   public long getLong(Metrics metrics) {
      return metrics.getAlertMetric().getLong(m_name, 0);
   }

   public String getName() {
      return m_name;
   }

   public String getString(Metrics metrics) {
      return metrics.getAlertMetric().getString(m_name, null);
   }

   public double getValue(Metrics metrics) {
      return metrics.getAlertMetric().getDouble(m_name, 0);
   }
}
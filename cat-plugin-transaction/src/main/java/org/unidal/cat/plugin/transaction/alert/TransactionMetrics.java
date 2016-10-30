package org.unidal.cat.plugin.transaction.alert;

import org.unidal.cat.core.alert.metric.AbstractMetrics;
import org.unidal.cat.core.alert.model.entity.AlertMetric;

public class TransactionMetrics extends AbstractMetrics {
   private String m_domain;

   private String m_type;

   private String m_name;

   private long m_total;

   private long m_fail;

   private double m_min;

   private double m_max;

   private double m_duration;

   public TransactionMetrics(AlertMetric metric) {
      super(metric);

      if (metric != null) {
         m_domain = getString("domain");
         m_type = getString("type");
         m_name = getString("name");
         m_total = getLong("total", 0);
         m_fail = getLong("fail", 0);
         m_min = getDouble("min", 0);
         m_max = getDouble("max", 0);
         m_duration = getDouble("duration", 0);
      }
   }

   public String getDomain() {
      return m_domain;
   }

   public double getDuration() {
      return m_duration;
   }

   public long getFail() {
      return m_fail;
   }

   public double getMax() {
      return m_max;
   }

   public double getMin() {
      return m_min;
   }

   public String getName() {
      return m_name;
   }

   public long getTotal() {
      return m_total;
   }

   public String getType() {
      return m_type;
   }

   public double getValue(String field) {
      return getDouble(field, 0);
   }
}

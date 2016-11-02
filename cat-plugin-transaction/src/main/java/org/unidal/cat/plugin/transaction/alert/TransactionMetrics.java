package org.unidal.cat.plugin.transaction.alert;

import static org.unidal.cat.plugin.transaction.alert.TransactionField.DOMAIN;
import static org.unidal.cat.plugin.transaction.alert.TransactionField.DURATION;
import static org.unidal.cat.plugin.transaction.alert.TransactionField.FAILS;
import static org.unidal.cat.plugin.transaction.alert.TransactionField.HITS;
import static org.unidal.cat.plugin.transaction.alert.TransactionField.NAME;
import static org.unidal.cat.plugin.transaction.alert.TransactionField.TYPE;

import org.unidal.cat.core.alert.metric.AbstractMetrics;
import org.unidal.cat.core.alert.model.entity.AlertMetric;

public class TransactionMetrics extends AbstractMetrics {
   private String m_domain;

   private String m_type;

   private String m_name;

   private long m_hits;

   private long m_fails;

   private double m_duration;

   public TransactionMetrics(AlertMetric metric) {
      super(metric);

      if (metric != null) {
         m_domain = DOMAIN.getString(this);
         m_type = TYPE.getString(this);
         m_name = NAME.getString(this);
         m_hits = HITS.getLong(this);
         m_fails = FAILS.getLong(this);
         m_duration = DURATION.getDouble(this);
      }
   }

   public String getDomain() {
      return m_domain;
   }

   public double getDuration() {
      return m_duration;
   }

   public long getFails() {
      return m_fails;
   }

   public long getHits() {
      return m_hits;
   }

   public String getName() {
      return m_name;
   }

   public String getType() {
      return m_type;
   }
}

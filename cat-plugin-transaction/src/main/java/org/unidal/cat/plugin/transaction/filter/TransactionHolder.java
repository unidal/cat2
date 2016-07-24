package org.unidal.cat.plugin.transaction.filter;

import org.unidal.cat.plugin.transaction.model.entity.Duration;
import org.unidal.cat.plugin.transaction.model.entity.Machine;
import org.unidal.cat.plugin.transaction.model.entity.Range;
import org.unidal.cat.plugin.transaction.model.entity.TransactionName;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.model.entity.TransactionType;

public class TransactionHolder {
   private TransactionReport m_report;

   private Machine m_machine;

   private TransactionType m_type;

   private TransactionName m_name;

   private Range m_range;

   private Duration m_duration;

   public Duration getDuration() {
      return m_duration;
   }

   public Machine getMachine() {
      return m_machine;
   }

   public TransactionName getName() {
      return m_name;
   }

   public Range getRange() {
      return m_range;
   }

   public TransactionReport getReport() {
      return m_report;
   }

   public TransactionType getType() {
      return m_type;
   }

   public void setDuration(Duration duration) {
      m_duration = duration;
   }

   public void setMachine(Machine machine) {
      m_machine = machine;
   }

   public void setName(TransactionName name) {
      m_name = name;
   }

   public void setRange(Range range) {
      m_range = range;
   }

   public void setReport(TransactionReport report) {
      m_report = report;
   }

   public void setType(TransactionType type) {
      m_type = type;
   }
}

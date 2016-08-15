package org.unidal.cat.plugin.transactions;

import org.unidal.cat.plugin.transactions.model.entity.TransactionsDepartment;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsDomain;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsDuration;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsName;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsRange;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsType;

public class TransactionsHolder {
   private TransactionsReport m_report;

   private TransactionsDepartment m_department;

   private TransactionsType m_type;

   private TransactionsName m_name;

   private TransactionsDomain m_domain;

   private TransactionsRange m_range;

   private TransactionsDuration m_duration;

   public TransactionsDepartment getDepartment() {
      return m_department;
   }

   public TransactionsDomain getDomain() {
      return m_domain;
   }

   public TransactionsDuration getDuration() {
      return m_duration;
   }

   public TransactionsName getName() {
      return m_name;
   }

   public TransactionsRange getRange() {
      return m_range;
   }

   public TransactionsReport getReport() {
      return m_report;
   }

   public TransactionsType getType() {
      return m_type;
   }

   public void setDepartment(TransactionsDepartment department) {
      m_department = department;
   }

   public void setDomain(TransactionsDomain domain) {
      m_domain = domain;
   }

   public void setDuration(TransactionsDuration duration) {
      m_duration = duration;
   }

   public void setName(TransactionsName name) {
      m_name = name;
   }

   public void setRange(TransactionsRange range) {
      m_range = range;
   }

   public void setReport(TransactionsReport report) {
      m_report = report;
   }

   public void setType(TransactionsType type) {
      m_type = type;
   }
}

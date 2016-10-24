package org.unidal.cat.plugin.transaction.alert;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.alert.metric.handler.TimeWindow;

public class TransactionTimeWindow implements TimeWindow<TransactionMetrics> {
   private Entry[] m_array;

   private String m_field;

   public TransactionTimeWindow(int size, String field) {
      m_array = new Entry[size];
      m_field = field;
   }

   @Override
   public void addMetrics(TransactionMetrics metrics) {
      Entry entry = m_array[0];

      if (entry == null) {
         entry = new Entry(m_field);
         m_array[0] = entry;
      }

      entry.add(metrics);
   }

   @Override
   public int getLength() {
      return m_array.length;
   }

   @Override
   public double getValue(int index) {
      Entry entry = m_array[index];

      if (entry != null) {
         return entry.getValue();
      } else {
         return 0;
      }
   }

   @Override
   public void reset() {
      int len = m_array.length;

      m_array = new Entry[len];
   }

   @Override
   public void shift() {
      for (int i = m_array.length - 1; i > 0; i--) {
         m_array[i] = m_array[i - 1];
      }

      m_array[0] = null;
   }

   @Override
   public String toString() {
      return String.format("%s[field=%s, size=%s]", getClass().getSimpleName(), m_field, m_array.length);
   }

   static class Entry {
      private String m_field;

      private List<TransactionMetrics> m_list = new ArrayList<TransactionMetrics>();

      public Entry(String field) {
         m_field = field;
      }

      public void add(TransactionMetrics metrics) {
         m_list.add(metrics);
      }

      public double getValue() {
         double value = 0;

         for (TransactionMetrics metrics : m_list) {
            value += metrics.getValue(m_field);
         }

         return value;
      }

      @Override
      public String toString() {
         return m_list.toString();
      }
   }
}
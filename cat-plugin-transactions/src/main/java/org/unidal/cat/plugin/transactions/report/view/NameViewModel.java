package org.unidal.cat.plugin.transactions.report.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.cat.core.report.view.PieChart;
import org.unidal.cat.core.report.view.TableViewModel;
import org.unidal.cat.plugin.transactions.filter.TransactionsHelper;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsName;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsReport;
import org.unidal.cat.plugin.transactions.model.entity.TransactionsType;
import org.unidal.cat.plugin.transactions.model.transform.BaseVisitor;
import org.unidal.cat.plugin.transactions.report.view.NameViewModel.NameEntry;
import org.unidal.helper.Splitters;

public class NameViewModel implements TableViewModel<NameEntry> {
   private String m_bu;

   private String m_type;

   private String m_query;

   private String m_sortBy;

   private List<NameEntry> m_entries = new ArrayList<NameEntry>();

   private PieChart m_chart = new PieChart();

   public NameViewModel(TransactionsReport report, String bu, String type, String query, String sortBy) {
      m_bu = bu;
      m_type = type;
      m_query = query;
      m_sortBy = sortBy;

      NameHarvester harvester = new NameHarvester();

      report.accept(harvester);
      harvester.harvest(m_entries);

      // sort them
      if (m_sortBy != null && m_sortBy.length() > 0) {
         NameComparator comparator = new NameComparator(m_sortBy);

         Collections.sort(m_entries, comparator);
      }

      // pie chart
      for (NameEntry entry : m_entries) {
         if (!entry.isSummary()) {
            m_chart.addItem(entry.getId(), entry.getTotal());
         }
      }

      m_chart.prepare();
   }

   public PieChart getPieChart() {
      return m_chart;
   }

   @Override
   public int getCount() {
      return m_entries.size() - 1;
   }

   public String getBu() {
      return m_bu;
   }

   public String getQuery() {
      return m_query;
   }

   @Override
   public List<NameEntry> getRows() {
      return m_entries;
   }

   public String getSortedBy() {
      return m_sortBy;
   }

   public long getTotal() {
      long total = 0;

      for (NameEntry entry : m_entries) {
         if (!entry.isSummary()) {
            total += entry.getTotal();
         }
      }

      return total;
   }

   public String getType() {
      return m_type;
   }

   private static class NameComparator implements Comparator<NameEntry> {
      private String m_sortBy;

      public NameComparator(String sortBy) {
         m_sortBy = sortBy;
      }

      @Override
      public int compare(NameEntry e1, NameEntry e2) {
         // keep summary on top
         if (e2.isSummary()) {
            return 1;
         } else if (e1.isSummary()) {
            return -1;
         }

         if (m_sortBy.equals("id")) {
            return e1.getId().compareTo(e2.getId());
         } else if (m_sortBy.equals("total")) {
            return (int) (e2.getTotal() - e1.getTotal());
         } else if (m_sortBy.equals("failure")) {
            return (int) (e2.getFailure() - e1.getFailure());
         } else if (m_sortBy.equals("failurePercent")) {
            return Double.compare(e2.getFailurePercent(), e1.getFailurePercent());
         } else if (m_sortBy.equals("avg")) {
            return Double.compare(e2.getAvg(), e1.getAvg());
         } else if (m_sortBy.equals("95line")) {
            return Double.compare(e2.getLine95(), e1.getLine95());
         } else if (m_sortBy.equals("99line")) {
            return Double.compare(e2.getLine99(), e1.getLine99());
         } else if (m_sortBy.equals("min")) {
            return Double.compare(e2.getMin(), e1.getMin());
         } else if (m_sortBy.equals("max")) {
            return Double.compare(e2.getMax(), e1.getMax());
         } else if (m_sortBy.equals("std")) {
            return Double.compare(e2.getStd(), e1.getStd());
         } else {
            return 0; // No comparation
         }
      }
   }

   public static class NameEntry {
      private TransactionsName m_name;

      private boolean m_summary;

      public NameEntry(TransactionsName name) {
         m_name = name;
      }

      public NameEntry(TransactionsName name, boolean summary) {
         m_name = name;
         m_summary = summary;
      }

      public double getAvg() {
         return m_name.getAvg();
      }

      public long getFailure() {
         return m_name.getFailCount();
      }

      public double getFailurePercent() {
         long total = m_name.getTotalCount();

         if (total > 0) {
            return m_name.getFailCount() / total;
         } else {
            return 0;
         }
      }

      public String getId() {
         return m_name.getId();
      }

      public double getLine95() {
         return m_name.getLine95Value();
      }

      public double getLine99() {
         return m_name.getLine99Value();
      }

      public double getMax() {
         return m_name.getMax();
      }

      public double getMin() {
         return m_name.getMin();
      }

      public String getSampleMessageId() {
         String failure = m_name.getFailMessageUrl();
         String success = m_name.getSuccessMessageUrl();

         if (failure != null) {
            return failure;
         } else {
            return success;
         }
      }

      public double getStd() {
         double std = std(m_name.getTotalCount(), m_name.getAvg(), m_name.getSum2(), m_name.getMax());

         return std;
      }

      public long getTotal() {
         return m_name.getTotalCount();
      }

      public double getTps() {
         return m_name.getTps();
      }

      public boolean isSummary() {
         return m_summary;
      }

      private double std(long count, double avg, double sum2, double max) {
         double value = sum2 / count - avg * avg;

         if (value <= 0 || count <= 1) {
            return 0;
         } else if (count == 2) {
            return max - avg;
         } else {
            return Math.sqrt(value);
         }
      }
   }

   private class NameHarvester extends BaseVisitor {
      private Map<String, List<TransactionsName>> m_map = new HashMap<String, List<TransactionsName>>();

      private QueryFilter m_filter = new QueryFilter(m_query);

      public void harvest(List<NameEntry> names) {
         TransactionsHelper helper = new TransactionsHelper();
         TransactionsName summary = new TransactionsName("SUMMARY");

         for (Map.Entry<String, List<TransactionsName>> e : m_map.entrySet()) {
            List<TransactionsName> list = e.getValue();
            TransactionsName name;

            if (list.size() == 1) {
               name = list.get(0);
            } else {
               name = new TransactionsName(e.getKey());

               for (TransactionsName item : list) {
                  helper.mergeName(name, item);
               }
            }

            helper.mergeName(summary, name);
            names.add(new NameEntry(name));
         }

         names.add(0, new NameEntry(summary, true));
      }

      @Override
      public void visitName(TransactionsName name) {
         if (!m_filter.apply(name)) {
            return;
         }

         String id = name.getId();
         List<TransactionsName> list = m_map.get(id);

         if (list == null) {
            list = new ArrayList<TransactionsName>();
            m_map.put(id, list);
         }

         list.add(name);
      }

      @Override
      public void visitType(TransactionsType type) {
         if (m_type == null || m_type.equals(type.getId())) {
            super.visitType(type);
         }
      }
   }

   private static class QueryFilter {
      private List<QueryRule> m_rules = new ArrayList<QueryRule>();

      public QueryFilter(String query) {
         if (query != null && query.length() > 0) {
            List<String> parts = Splitters.by(' ').trim().noEmptyItem().split(query);

            for (String part : parts) {
               m_rules.add(new QueryRule(part));
            }
         }
      }

      public boolean apply(TransactionsName name) {
         for (QueryRule rule : m_rules) {
            if (!rule.apply(name)) {
               return false;
            }
         }

         return true;
      }
   }

   private static class QueryRule {
      private String m_field;

      private char m_op = '~';

      private List<String> m_args;

      public QueryRule(String part) {
         int pos = part.indexOf(':');

         if (pos > 0) {
            m_field = part.substring(0, pos);
            part = part.substring(pos + 1);
         }

         if (part.startsWith("=")) {
            m_op = '='; // EQ
            m_args = Splitters.by('|').trim().noEmptyItem().split(part.substring(1));
         } else if (part.startsWith("~")) {
            m_op = '~'; // LIKE
            m_args = Splitters.by('|').trim().noEmptyItem().split(part.substring(1));
         } else {
            m_op = '~'; // LIKE
            m_args = Splitters.by('|').trim().noEmptyItem().split(part);
         }
      }

      public boolean apply(TransactionsName name) {
         if (m_op == '~') {
            if (m_field == null || "name".equals(m_field)) {
               for (String part : m_args) {
                  if (name.getId().contains(part)) {
                     return true;
                  }
               }

               return false;
            }
         } else if (m_op == '=') {
            if (m_field == null || "name".equals(m_field)) {
               for (String part : m_args) {
                  if (name.getId().equals(part)) {
                     return true;
                  }
               }

               return false;
            }
         }

         return false;
      }
   }
}
package org.unidal.cat.plugin.events.report.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.cat.core.view.TableViewModel;
import org.unidal.cat.plugin.events.filter.EventsHelper;
import org.unidal.cat.plugin.events.model.entity.EventsReport;
import org.unidal.cat.plugin.events.model.entity.EventsType;
import org.unidal.cat.plugin.events.model.transform.BaseVisitor;
import org.unidal.cat.plugin.events.report.view.TypeViewModel.TypeEntry;
import org.unidal.helper.Splitters;

public class TypeViewModel implements TableViewModel<TypeEntry> {
   private String m_bu;

   private String m_query;

   private String m_sortBy;

   private List<TypeEntry> m_entries = new ArrayList<TypeEntry>();

   public TypeViewModel(EventsReport report, String bu, String query, String sortBy) {
      m_bu = bu;
      m_query = query;
      m_sortBy = sortBy;

      TypeHarvester harvester = new TypeHarvester();

      report.accept(harvester);
      harvester.harvest(m_entries);

      if (m_sortBy != null && m_sortBy.length() > 0) {
         TypeComparator comparator = new TypeComparator(m_sortBy);

         Collections.sort(m_entries, comparator);
      }
   }

   @Override
   public int getCount() {
      return m_entries.size();
   }

   public String getBu() {
      return m_bu;
   }

   public String getQuery() {
      return m_query;
   }

   @Override
   public List<TypeEntry> getRows() {
      return m_entries;
   }

   public String getSortedBy() {
      return m_sortBy;
   }

   public long getTotal() {
      long total = 0;

      for (TypeEntry entry : m_entries) {
         total += entry.getTotal();
      }

      return total;
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

      public boolean apply(EventsType name) {
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

      public boolean apply(EventsType name) {
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

   private static class TypeComparator implements Comparator<TypeEntry> {
      private String m_sortBy;

      public TypeComparator(String sortBy) {
         m_sortBy = sortBy;
      }

      @Override
      public int compare(TypeEntry e1, TypeEntry e2) {
         if (m_sortBy.equals("id")) {
            return e1.getId().compareTo(e2.getId());
         } else if (m_sortBy.equals("total")) {
            return (int) (e2.getTotal() - e1.getTotal());
         } else if (m_sortBy.equals("failure")) {
            return (int) (e2.getFailure() - e1.getFailure());
         } else if (m_sortBy.equals("failurePercent")) {
            return Double.compare(e2.getFailurePercent(), e1.getFailurePercent());
         } else {
            return 0; // No comparation
         }
      }
   }

   public static class TypeEntry {
      private EventsType m_name;

      public TypeEntry(EventsType name) {
         m_name = name;
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

      public String getSampleMessageId() {
         String failure = m_name.getFailMessageUrl();
         String success = m_name.getSuccessMessageUrl();

         if (failure != null) {
            return failure;
         } else {
            return success;
         }
      }

      public long getTotal() {
         return m_name.getTotalCount();
      }

      public double getTps() {
         return m_name.getTps();
      }
   }

   private class TypeHarvester extends BaseVisitor {
      private Map<String, List<EventsType>> m_map = new HashMap<String, List<EventsType>>();

      private QueryFilter m_filter = new QueryFilter(m_query);

      public void harvest(List<TypeEntry> names) {
         EventsHelper helper = new EventsHelper();

         for (Map.Entry<String, List<EventsType>> e : m_map.entrySet()) {
            List<EventsType> list = e.getValue();
            EventsType type;

            if (list.size() == 1) {
               type = list.get(0);
            } else {
               type = new EventsType(e.getKey());

               for (EventsType item : list) {
                  helper.mergeType(type, item);
               }
            }

            names.add(new TypeEntry(type));
         }
      }

      @Override
      public void visitType(EventsType type) {
         if (!m_filter.apply(type)) {
            return;
         }

         String id = type.getId();
         List<EventsType> list = m_map.get(id);

         if (list == null) {
            list = new ArrayList<EventsType>();
            m_map.put(id, list);
         }

         list.add(type);
      }
   }
}
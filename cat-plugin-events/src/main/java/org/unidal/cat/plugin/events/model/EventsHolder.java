package org.unidal.cat.plugin.events.model;

import org.unidal.cat.plugin.events.model.entity.EventsDepartment;
import org.unidal.cat.plugin.events.model.entity.EventsDomain;
import org.unidal.cat.plugin.events.model.entity.EventsName;
import org.unidal.cat.plugin.events.model.entity.EventsRange;
import org.unidal.cat.plugin.events.model.entity.EventsReport;
import org.unidal.cat.plugin.events.model.entity.EventsType;

public class EventsHolder {
   private EventsReport m_report;

   private EventsDepartment m_department;

   private EventsType m_type;

   private EventsName m_name;

   private EventsDomain m_domain;

   private EventsRange m_range;

   public EventsDepartment getDepartment() {
      return m_department;
   }

   public EventsDomain getDomain() {
      return m_domain;
   }

   public EventsName getName() {
      return m_name;
   }

   public EventsRange getRange() {
      return m_range;
   }

   public EventsReport getReport() {
      return m_report;
   }

   public EventsType getType() {
      return m_type;
   }

   public void setDepartment(EventsDepartment department) {
      m_department = department;
   }

   public void setDomain(EventsDomain domain) {
      m_domain = domain;
   }

   public void setName(EventsName name) {
      m_name = name;
   }

   public void setRange(EventsRange range) {
      m_range = range;
   }

   public void setReport(EventsReport report) {
      m_report = report;
   }

   public void setType(EventsType type) {
      m_type = type;
   }
}

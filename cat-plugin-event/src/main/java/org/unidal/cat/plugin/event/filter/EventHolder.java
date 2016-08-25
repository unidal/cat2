package org.unidal.cat.plugin.event.filter;

import org.unidal.cat.plugin.event.model.entity.EventName;
import org.unidal.cat.plugin.event.model.entity.EventReport;
import org.unidal.cat.plugin.event.model.entity.EventType;
import org.unidal.cat.plugin.event.model.entity.Machine;
import org.unidal.cat.plugin.event.model.entity.Range;

public class EventHolder {
   private EventReport m_report;

   private Machine m_machine;

   private EventType m_type;

   private EventName m_name;

   private Range m_range;

   public Machine getMachine() {
      return m_machine;
   }

   public EventName getName() {
      return m_name;
   }

   public Range getRange() {
      return m_range;
   }

   public EventReport getReport() {
      return m_report;
   }

   public EventType getType() {
      return m_type;
   }

   public void setMachine(Machine machine) {
      m_machine = machine;
   }

   public void setName(EventName name) {
      m_name = name;
   }

   public void setRange(Range range) {
      m_range = range;
   }

   public void setReport(EventReport report) {
      m_report = report;
   }

   public void setType(EventType type) {
      m_type = type;
   }
}

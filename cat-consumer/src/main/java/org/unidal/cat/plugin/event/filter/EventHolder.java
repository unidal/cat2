package org.unidal.cat.plugin.event.filter;

import com.dianping.cat.consumer.event.model.entity.*;

public class EventHolder {
    private EventReport m_report;

    private Machine m_machine;

    private EventType m_type;

    private EventName m_name;

    private Range m_range;

    public EventReport getReport() {
        return m_report;
    }

    public void setReport(EventReport report) {
        m_report = report;
    }

    public Machine getMachine() {
        return m_machine;
    }

    public void setMachine(Machine machine) {
        m_machine = machine;
    }

    public EventType getType() {
        return m_type;
    }

    public void setType(EventType type) {
        m_type = type;
    }

    public EventName getName() {
        return m_name;
    }

    public void setName(EventName name) {
        m_name = name;
    }

    public Range getRange() {
        return m_range;
    }

    public void setRange(Range range) {
        m_range = range;
    }
}
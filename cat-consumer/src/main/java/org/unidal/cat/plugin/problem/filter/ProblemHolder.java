package org.unidal.cat.plugin.problem.filter;

import com.dianping.cat.consumer.problem.model.entity.*;

public class ProblemHolder {
    private ProblemReport m_report;

    private Machine m_machine;

    private Entity m_entity;

    private JavaThread m_javaThread;

    private Duration m_duration;

    public ProblemReport getReport() {
        return m_report;
    }

    public Machine getMachine() {
        return m_machine;
    }

    public Entity getEntity() {
        return m_entity;
    }

    public JavaThread getJavaThread() {
        return m_javaThread;
    }

    public Duration getDuration() {
        return m_duration;
    }

    public void setReport(ProblemReport report) {
        this.m_report = report;
    }

    public void setMachine(Machine machine) {
        this.m_machine = machine;
    }

    public void setEntity(Entity entity) {
        this.m_entity = entity;
    }

    public void setJavaThread(JavaThread javaThread) {
        this.m_javaThread = javaThread;
    }

    public void setDuration(Duration duration) {
        this.m_duration = duration;
    }
}

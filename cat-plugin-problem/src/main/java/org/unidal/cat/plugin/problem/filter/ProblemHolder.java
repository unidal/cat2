package org.unidal.cat.plugin.problem.filter;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;

public class ProblemHolder {
   private ProblemReport m_report;

   private Machine m_machine;

   private Entity m_entity;

   private JavaThread m_javaThread;

   private Duration m_duration;

   public Duration getDuration() {
      return m_duration;
   }

   public Entity getEntity() {
      return m_entity;
   }

   public JavaThread getJavaThread() {
      return m_javaThread;
   }

   public Machine getMachine() {
      return m_machine;
   }

   public ProblemReport getReport() {
      return m_report;
   }

   public void setDuration(Duration duration) {
      this.m_duration = duration;
   }

   public void setEntity(Entity entity) {
      this.m_entity = entity;
   }

   public void setJavaThread(JavaThread javaThread) {
      this.m_javaThread = javaThread;
   }

   public void setMachine(Machine machine) {
      this.m_machine = machine;
   }

   public void setReport(ProblemReport report) {
      this.m_report = report;
   }
}

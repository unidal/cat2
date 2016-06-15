package org.unidal.cat.plugin.problem;

import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.message.spi.MessageTree;
import org.unidal.cat.spi.analysis.AbstractMessageAnalyzer;
import org.unidal.cat.spi.analysis.MessageAnalyzer;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.List;

@Named(type = MessageAnalyzer.class, value = ProblemConstants.NAME, instantiationStrategy = Named.PER_LOOKUP)
public class ProblemReportAnalyzer extends AbstractMessageAnalyzer<ProblemReport> {
   @Inject({DefaultAbstractProblemHandler.ID, LongExecutionAbstractProblemHandler.ID})
   private List<ProblemHandler> m_handlers;

   @Override
   protected void process(MessageTree tree) {
      String domain = tree.getDomain();
      ProblemReport report = getLocalReport(domain);

      report.addIp(tree.getIpAddress());
      Machine machine = report.findOrCreateMachine(tree.getIpAddress());

      for (ProblemHandler handler : m_handlers) {
         handler.handle(machine, tree);
      }
   }
}

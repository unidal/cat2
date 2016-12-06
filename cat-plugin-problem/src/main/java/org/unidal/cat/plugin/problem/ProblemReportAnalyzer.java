package org.unidal.cat.plugin.problem;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.spi.analysis.AbstractMessageAnalyzer;
import org.unidal.cat.spi.analysis.MessageAnalyzer;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageAnalyzer.class, value = ProblemConstants.NAME, instantiationStrategy = Named.PER_LOOKUP)
public class ProblemReportAnalyzer extends AbstractMessageAnalyzer<ProblemReport> implements Initializable {
   private List<ProblemHandler> m_handlers;

   @Override
   public void initialize() throws InitializationException {
      m_handlers = new ArrayList<ProblemHandler>(lookupList(ProblemHandler.class));
   }

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

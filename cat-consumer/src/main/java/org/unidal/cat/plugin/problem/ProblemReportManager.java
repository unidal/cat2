package org.unidal.cat.plugin.problem;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.report.internals.AbstractReportManager;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportManager.class, value = ProblemConstants.NAME)
public class ProblemReportManager extends AbstractReportManager<ProblemReport> {
    @Override
    public int getThreadsCount() {
        return 2;
    }
}
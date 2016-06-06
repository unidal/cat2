package org.unidal.cat.plugin.problem;

import com.dianping.cat.consumer.problem.ProblemReportMerger;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.lookup.annotation.Named;

import java.util.Collection;

@Named(type = ReportAggregator.class, value = ProblemConstants.NAME)
public class ProblemReportAggregator implements ReportAggregator<ProblemReport> {
    @Override
    public ProblemReport aggregate(ReportPeriod period, Collection<ProblemReport> reports) {
        ProblemReport aggregated = new ProblemReport();

        if (reports.size() > 0) {
            ProblemReportMerger merger = new ProblemReportMerger(aggregated);

            // must be same domain
            aggregated.setDomain(reports.iterator().next().getDomain());

            for (ProblemReport report : reports) {
                report.accept(merger);
            }
        }

        return aggregated;
    }

    @Override
    public ProblemReport makeAll(ReportPeriod period, Collection<ProblemReport> reports) {
        return null;
    }
}

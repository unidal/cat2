package org.unidal.cat.plugin.transaction;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.ReportManagerManager;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.analysis.pipeline.AbstractPipeline;
import org.unidal.cat.spi.analysis.pipeline.Pipeline;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.cat.spi.report.internals.ReportDelegateManager;
import org.unidal.lookup.annotation.Named;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Named(type = Pipeline.class, value = TransactionConstants.NAME, instantiationStrategy = Named.PER_LOOKUP)
public class TransactionPipeline extends AbstractPipeline {
    @Override
    protected void beforeCheckpoint() {
        ReportManagerManager rmm = super.lookup(ReportManagerManager.class);
        ReportManager<Report> reportManager = rmm.getReportManager(getName());

        ReportDelegateManager rdg = super.lookup(ReportDelegateManager.class);
        ReportDelegate<Report> reportDelegate = rdg.getDelegate(getName());


        try {
            List<Map<String, Report>> reportMapList = reportManager.getLocalReports(ReportPeriod.HOUR, getHour());
            if(reportMapList.size() > 0){
                List<Report> reportList = new ArrayList<Report>();

                for (Map<String, Report> map : reportMapList) {
                    for (Report report : map.values()) {
                        reportList.add(report);
                    }
                }

                Report allReport = reportDelegate.makeAllReport(ReportPeriod.HOUR, reportList);

                Map<String, Report> map = reportMapList.get(0);
                map.put(Constants.ALL, allReport);
            }
        } catch (IOException e) {
            Cat.logError(e);
        }
    }
}

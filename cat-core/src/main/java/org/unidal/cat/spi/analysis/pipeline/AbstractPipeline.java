package org.unidal.cat.spi.analysis.pipeline;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.analysis.MessageAnalyzer;
import org.unidal.cat.spi.analysis.MessageAnalyzerManager;
import org.unidal.cat.spi.analysis.event.TimeWindowHandler;
import org.unidal.cat.spi.analysis.event.TimeWindowManager;
import org.unidal.cat.spi.report.ReportAggregator;
import org.unidal.cat.spi.report.ReportAggregatorUtil;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map;

/**
 * Created by yj.huang on 16-5-9.
 */
public abstract class AbstractPipeline implements TimeWindowHandler, Initializable {
    @Inject
    private MessageAnalyzerManager m_analyzerManager;

    @Inject
    private TimeWindowManager m_timeWindowManager;

    @Inject
    private ReportAggregator m_aggregator;

    private ExecutorService executor = Threads.forPool().getFixedThreadPool("pipeline", 32);

    // Process once after doCheckPoint() is done for a group of analyzers
    public abstract void processPerAnalyzerGroup(Map<String, ? extends Report> reports);

    // Process once after doCheckPoint() is done for all analyzers
    public abstract void processAllReports(Map<String, ? extends Report> globalReportMap);

    @Override
    public void initialize() throws InitializationException {
        m_timeWindowManager.register(this);
    }

    @Override
    public void onTimeWindowEnter(int hour) {

    }

    //	@Override
//	public void doCheckpoint(int hour, boolean atEnd) {
//		List<MessageAnalyzer> analyzers = m_analyzers.remove(hour);
//
//		if (analyzers != null) {
//			for (MessageAnalyzer analyzer : analyzers) {
//				try {
//					analyzer.doCheckpoint(atEnd);
//				} catch (IOException e) {
//					m_logger.error(String.format("Error when doing checkpoint of %s!", analyzer), e);
//				}
//			}
//		}
//	}

    @Override
    public void onTimeWindowExit(int hour) {
        List<MessageAnalyzer> analyzers = m_analyzerManager.removeAnalyzers(hour);
        if (null == analyzers || analyzers.isEmpty())
            return;

        final List<Map<String, ? extends Report>> groupAnalyzerReportMaps = new ArrayList<Map<String, ? extends Report>>(analyzers.size());
        // For each group analyzer, doCheckPoint() and wait for its end event.
        for (final MessageAnalyzer analyzer : analyzers) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        analyzer.doCheckpoint(true);

                        // Event notification after doCheckPoint() done for a group analyzer.
                        // Because analyzer is a group analyzer,
                        // it will aggregate reports from all transaction analyzer instances, in analyzer.getLocalReports()
                        Map<String, ? extends Report> groupAnalyzerReports = analyzer.getLocalReports();
                        groupAnalyzerReportMaps.add(groupAnalyzerReports);
                        processPerAnalyzerGroup(groupAnalyzerReports);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(20, TimeUnit.MINUTES);

            // Event notification after doCheckPoint() done for all analyzers.
            // A global report map of domain ==> reports from all group analyzers of all analyzers.
            Map<String, ? extends Report> globalReportMap = ReportAggregatorUtil.aggregateMapsOfReports(m_aggregator, groupAnalyzerReportMaps);
            processAllReports(globalReportMap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

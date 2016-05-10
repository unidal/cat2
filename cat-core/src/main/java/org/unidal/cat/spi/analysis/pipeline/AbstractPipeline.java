package org.unidal.cat.spi.analysis.pipeline;

import com.dianping.cat.message.spi.MessageTree;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.ReportManagerManager;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.analysis.MessageRoutingStrategy;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.RoleHintEnabled;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.Map;

public class AbstractPipeline<T extends Report> implements Pipeline, RoleHintEnabled {
    @Inject
    private ReportManagerManager m_rmm;

    @Inject
    private MessageRoutingStrategy m_strategy;

    private String m_name;

    private int m_hour;


    @Override
    public void initialize(int hour) {
        m_hour = hour;
    }

    @Override
    public boolean analyze(MessageTree tree) {
        return false;
    }

    @Override
    public void checkpoint() throws IOException {
        beforeCheckpoint();
        doCheckpoint(true);
        afterCheckpoint();
    }

    protected String getName() {
        return m_name;
    }

    protected MessageRoutingStrategy getRoutingStrategy() {
        return m_strategy;
    }

    protected void beforeCheckpoint() throws IOException {

    }

    protected void doCheckpoint(boolean atEnd) throws IOException {
        ReportManager reportManager = m_rmm.getReportManager(m_name);
        int size = getAnalyzerSize();
        for (int i = 0; i < size; i++) {
            reportManager.doCheckpoint(new Date(TimeUnit.HOURS.toMillis(m_hour)), i, atEnd);
        }
    }

    protected void afterCheckpoint() {

    }

    @Override
    public void enableRoleHint(String roleHint) {
        m_name = roleHint;
    }

    protected int getAnalyzerSize() {
        return 1;
    }
}

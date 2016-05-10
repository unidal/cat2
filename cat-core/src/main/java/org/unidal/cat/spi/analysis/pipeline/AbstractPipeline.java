package org.unidal.cat.spi.analysis.pipeline;

import com.dianping.cat.message.spi.MessageTree;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.ReportManagerManager;
import org.unidal.cat.spi.analysis.MessageAnalyzer;
import org.unidal.cat.spi.analysis.MessageRoutingStrategy;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.RoleHintEnabled;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractPipeline extends ContainerHolder implements Pipeline, RoleHintEnabled, LogEnabled {
    @Inject
    private ReportManagerManager m_rmm;

    @Inject
    private MessageRoutingStrategy m_strategy;

    private String m_name;

    private int m_hour;

    private Logger m_logger;

    List<MessageAnalyzer> m_analyzers = new ArrayList<MessageAnalyzer>();

    @Override
    public void initialize(int hour) {
        m_hour = hour;
        int size = getAnalyzerSize();
        for (int i = 0; i < size; i++) {
            MessageAnalyzer analyzer = lookup(MessageAnalyzer.class, m_name);
            try {
                analyzer.initialize(i, hour);
                Threads.forGroup("Cat").start(analyzer);
                m_analyzers.add(analyzer);
            } catch (Throwable e) {
                String msg = String.format("Error when starting %s!", analyzer);
                e.printStackTrace();
                m_logger.error(msg, e);
            }
        }
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
        finish();
    }

    protected String getName() {
        return m_name;
    }

    protected MessageRoutingStrategy getRoutingStrategy() {
        return m_strategy;
    }

    abstract protected void beforeCheckpoint() throws IOException;

    protected void doCheckpoint(boolean atEnd) throws IOException {
        ReportManager reportManager = m_rmm.getReportManager(m_name);
        int size = getAnalyzerSize();
        for (int i = 0; i < size; i++) {
            reportManager.doCheckpoint(new Date(TimeUnit.HOURS.toMillis(m_hour)), i, atEnd);
        }
    }

    abstract protected void afterCheckpoint();

    protected void finish(){
        for(MessageAnalyzer messageAnalyzer : m_analyzers){
            super.release(messageAnalyzer);
        }
    }

    @Override
    public void enableRoleHint(String roleHint) {
        m_name = roleHint;
    }

    protected int getAnalyzerSize() {
        return 1;
    }

    @Override
    public void enableLogging(Logger logger) {
        m_logger = logger;
    }
}

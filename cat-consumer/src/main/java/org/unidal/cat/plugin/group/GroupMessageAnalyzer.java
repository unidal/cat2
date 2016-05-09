package org.unidal.cat.plugin.group;

import com.dianping.cat.Constants;
import com.dianping.cat.message.spi.MessageTree;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.spi.ReportConfiguration;
import org.unidal.cat.spi.analysis.MessageAnalyzer;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GroupMessageAnalyzer extends ContainerHolder implements MessageAnalyzer, LogEnabled {
    @Inject
    String m_type;

    @Inject
    private ReportConfiguration m_configuration;

    private static final ArrayList<String> roundRobinTask = new ArrayList<String>(Arrays.asList(Constants.DUMP));

    private int analyzerIndex;

    private Logger m_logger;

    private List<MessageAnalyzer> m_analyzers = new ArrayList<MessageAnalyzer>();

    private int length;

    private int m_hour;

    private int m_index;

    private boolean manyTasks = false;

    private boolean isRoundRobinTask = false;

    @Override
    public boolean handle(MessageTree tree) {
        int index = 0;
        if (manyTasks) {
            if (isRoundRobinTask) {
                index = analyzerIndex;
                analyzerIndex++;
            } else {
                index = Math.abs(tree.getDomain().hashCode()) % length;
            }
        }
        MessageAnalyzer analyzer = m_analyzers.get(index);
        return analyzer.handle(tree);
    }

    @Override
    public void doCheckpoint(boolean atEnd) throws IOException {
        for (MessageAnalyzer analyzer : m_analyzers){
            analyzer.doCheckpoint(atEnd);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void initialize(int index, int hour) throws IOException {
        m_hour = hour;
        m_index = index;
        int count = m_configuration.getAnanlyzerCount(m_type);
        for (int i = 0; i < count; i++) {
            MessageAnalyzer analyzer = lookup(MessageAnalyzer.class, m_type);

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
        length = count;
        manyTasks = length > 1;
        isRoundRobinTask = roundRobinTask.contains(m_type);
    }

    @Override
    public boolean isEligible(MessageTree tree){
        return m_analyzers.get(0).isEligible(tree);
    }

    @Override
    public void configure(Map<String, String> properties) {
    }


    @Override
    public String getName() {
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(TimeUnit.HOURS.toMillis(m_hour));
        return getClass().getSimpleName() + "-" + cal.get(Calendar.HOUR_OF_DAY) + "-" + m_type + "-" + m_index;
    }

    @Override
    public void shutdown() {
        for(MessageAnalyzer analyzer : m_analyzers){
            try {
                super.release(analyzer);
                analyzer.shutdown();
            } catch (Throwable e) {
                m_logger.error(String.format("Error when stopping %s!", analyzer), e);
            }
        }
    }

    @Override
    public void run() {

    }

    public void setType(String m_type) {
        this.m_type = m_type;
    }

    @Override
    public void enableLogging(Logger logger) {
        m_logger = logger;
    }
}

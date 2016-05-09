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
import org.unidal.lookup.extension.RoleHintEnabled;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GroupMessageAnalyzer extends ContainerHolder implements MessageAnalyzer, RoleHintEnabled, LogEnabled {
    @Inject
    String m_type;

    @Inject
    private ReportConfiguration m_configuration;

    private static final ArrayList<String> roundRobinTask = new ArrayList<String>(Arrays.asList(Constants.DUMP));

    private Logger m_logger;

    private List<MessageAnalyzer> m_analyzers = new ArrayList<MessageAnalyzer>();

    @Override
    public boolean handle(MessageTree tree) {

        // Pick one analyzer out of m_analyzers
        // call analyzer.handle(tree);
        return true;
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
    }

    @Override
    public boolean isEligible(MessageTree tree){
        return true;
    }

    @Override
    public void configure(Map<String, String> properties) {
    }


    @Override
    public String getName() {
        return null;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void run() {

    }

    public void setType(String m_type) {
        this.m_type = m_type;
    }

    @Override
    public void enableRoleHint(String roleHint) {

    }

    @Override
    public void enableLogging(Logger logger) {
        m_logger = logger;
    }
}

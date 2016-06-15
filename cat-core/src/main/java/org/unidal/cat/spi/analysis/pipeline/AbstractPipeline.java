package org.unidal.cat.spi.analysis.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.spi.ReportConfiguration;
import com.dianping.cat.Constants;
import com.dianping.cat.message.spi.MessageTree;
import org.unidal.cat.spi.analysis.MessageAnalyzer;
import org.unidal.cat.spi.analysis.MessageRoutingStrategy;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.RoleHintEnabled;

public abstract class AbstractPipeline extends ContainerHolder implements Pipeline, RoleHintEnabled, LogEnabled {
	@Inject(StrategyConstants.DOMAIN_HASH)
	private MessageRoutingStrategy m_strategy;

	@Inject
	private ReportConfiguration m_config;

	private String m_name;

	private int m_hour;

	private List<MessageAnalyzer> m_analyzers = new ArrayList<MessageAnalyzer>();

	private Logger m_logger;

	protected void afterCheckpoint() throws Exception {
		// to be overridden
	}

	@Override
	public boolean analyze(MessageTree tree) {
		MessageRoutingStrategy strategy = getRoutingStrategy();
		int index = strategy.getIndex(tree, m_analyzers.size());
		MessageAnalyzer analyzer = m_analyzers.get(index);

		return analyzer.handle(tree);
	}

	protected void beforeCheckpoint() throws Exception {
		// to be overridden
	}

	@Override
	public void checkpoint(boolean atEnd) throws Exception {
		beforeCheckpoint();
		doCheckpoint(atEnd);
		afterCheckpoint();
	}

	@Override
	public void destroy() {
		for (MessageAnalyzer messageAnalyzer : m_analyzers) {
			super.release(messageAnalyzer);
			messageAnalyzer.destroy();
		}
	}

	protected void doCheckpoint(final boolean atEnd) throws Exception {
		for (MessageAnalyzer analyzer : m_analyzers) {
			analyzer.doCheckpoint(atEnd);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void enableRoleHint(String roleHint) {
		m_name = roleHint;
	}

	protected int getHour() {
		return m_hour;
	}

	@Override
	public String getName() {
		return m_name;
	}

	protected MessageRoutingStrategy getRoutingStrategy() {
		return m_strategy;
	}

	@Override
	public void initialize(int hour) {
		m_hour = hour;

		int size = m_config.getAnanlyzerCount(m_name);

		for (int i = 0; i < size; i++) {
			MessageAnalyzer analyzer = lookup(MessageAnalyzer.class, m_name);

			try {
				analyzer.initialize(i, hour);
				m_analyzers.add(analyzer);
				Threads.forGroup("Cat").start(analyzer);
			} catch (Throwable e) {
				String msg = String.format("Error when initializing analyzer %s!", analyzer);

				m_logger.error(msg, e);
			}
		}
	}
}

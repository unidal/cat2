package org.unidal.cat.spi.analysis;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.spi.analysis.event.TimeWindowHandler;
import org.unidal.cat.spi.analysis.event.TimeWindowManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;

@Named(type = MessageDispatcher.class)
public class DefaultMessageDispatcher implements MessageDispatcher, TimeWindowHandler, Initializable {
	@Inject
	private MessageAnalyzerManager m_analyzerManager;

	@Inject
	private TimeWindowManager m_timeWindowManager;

	@Inject
	private ServerStatisticManager m_stateManager;

	private int m_currentHour;

	private List<MessageAnalyzer> m_currentAnalyzers;

	private List<MessageAnalyzer> m_lastAnalyzers;

	private void dispatch(List<MessageAnalyzer> analyzers, MessageTree tree) {
		for (MessageAnalyzer analyzer : analyzers) {
			String domain = tree.getDomain();

			m_stateManager.addMessageTotal(domain, 1);

            if (analyzer.isEligible(tree)){
                boolean success = analyzer.handle(tree);

                if (!success) {
                    m_stateManager.addMessageTotalLoss(domain, 1);
                }
            }
		}
	}

	@Override
	public void dispatch(MessageTree tree) {
		int hour = (int) TimeUnit.MILLISECONDS.toHours(tree.getMessage().getTimestamp());
		List<MessageAnalyzer> analyzers = null;

		synchronized (this) {
			if (hour == m_currentHour) {
				analyzers = m_currentAnalyzers;
			} else if (hour == m_currentHour - 1) {
				analyzers = m_lastAnalyzers;
			}
		}

		if (analyzers != null) {
			dispatch(analyzers, tree);
		} else {
			// discard it
			m_stateManager.addNetworkTimeError(1);
		}
	}

	@Override
	public void onTimeWindowExit(int hour) {
		// do nothing here
	}

	@Override
	public void onTimeWindowEnter(int hour) {
		List<MessageAnalyzer> currentAnalyzers = m_analyzerManager.getAnalyzers(hour);
		List<MessageAnalyzer> lastAnalyzers = m_analyzerManager.getAnalyzers(hour - 1);

		synchronized (this) {
			m_currentHour = hour;
			m_currentAnalyzers = currentAnalyzers;
			m_lastAnalyzers = lastAnalyzers;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_timeWindowManager.register(this);
	}
}
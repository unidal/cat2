package org.unidal.cat.spi.analysis;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.spi.analysis.event.TimeWindowHandler;
import org.unidal.cat.spi.analysis.event.TimeWindowManager;
import org.unidal.cat.spi.analysis.pipeline.Pipeline;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;

@Named(type = MessageDispatcher.class)
public class DefaultMessageDispatcher implements MessageDispatcher, TimeWindowHandler, Initializable {
	@Inject
	private PipelineManager m_pipelineManager;

	@Inject
	private TimeWindowManager m_timeWindowManager;

	@Inject
	private ServerStatisticManager m_stateManager;

	private int m_currentHour;

	private List<Pipeline> m_currentPipelines;

	private List<Pipeline> m_lastPipelines;

	private void dispatch(List<Pipeline> pipelines, MessageTree tree) {
		String domain = tree.getDomain();
		boolean hasFailure = false;
		for (Pipeline pipeline : pipelines) {
			m_stateManager.addMessageTotal(domain, 1);

			boolean success = pipeline.analyze(tree);

			if (!success) {
				hasFailure = true;
			}
		}

		// If multiple analyzers drop the same message tree, only one message will be counted as loss.
		if (hasFailure) {
			m_stateManager.addMessageTotalLoss(domain, 1);
		}
	}

	@Override
	public void dispatch(MessageTree tree) {
		int hour = (int) TimeUnit.MILLISECONDS.toHours(tree.getMessage().getTimestamp());
		List<Pipeline> pipelines = null;

		synchronized (this) {
			if (hour == m_currentHour) {
				pipelines = m_currentPipelines;
			} else if (hour == m_currentHour - 1) {
				pipelines = m_lastPipelines;
			}
		}

		if (pipelines != null) {
			dispatch(pipelines, tree);
		} else {
			// discard it
			m_stateManager.addNetworkTimeError(1);
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_timeWindowManager.register(this);
	}

	@Override
	public void onTimeWindowEnter(int hour) {
		List<Pipeline> currentPipelines = m_pipelineManager.getPipelines(hour);
		List<Pipeline> lastPipelines = m_pipelineManager.getPipelines(hour - 1);

		synchronized (this) {
			m_currentHour = hour;
			m_currentPipelines = currentPipelines;
			m_lastPipelines = lastPipelines;
		}
	}

	@Override
	public void onTimeWindowExit(int hour) {
		// do nothing here
	}
}
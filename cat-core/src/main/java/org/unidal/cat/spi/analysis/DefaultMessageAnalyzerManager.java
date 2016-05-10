package org.unidal.cat.spi.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.spi.analysis.event.TimeWindowHandler;
import org.unidal.cat.spi.analysis.event.TimeWindowManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = MessageAnalyzerManager.class)
public class DefaultMessageAnalyzerManager extends ContainerHolder implements MessageAnalyzerManager,
      TimeWindowHandler, Initializable, LogEnabled {
	@Inject
	private TimeWindowManager m_timeWindowManager;

	private Map<Integer, List<MessageAnalyzer>> m_analyzers = new HashMap<Integer, List<MessageAnalyzer>>();

	private List<String> m_names = new ArrayList<String>();

	private Logger m_logger;

    @Override
    public List<MessageAnalyzer> removeAnalyzers(int hour) {
        List<MessageAnalyzer> analyzers = m_analyzers.remove(hour);
        return analyzers;
    }

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public List<MessageAnalyzer> getAnalyzers(int hour) {
		List<MessageAnalyzer> analyzers = m_analyzers.get(hour);

		if (analyzers == null) {
			synchronized (m_analyzers) {
				analyzers = m_analyzers.get(hour);

				if (analyzers == null) {
					analyzers = new ArrayList<MessageAnalyzer>();

					for (String name : m_names) {

						if(name.contains("group")){
							MessageAnalyzer analyzer = lookup(MessageAnalyzer.class, name);
							try {
								analyzer.initialize(0,hour);
								analyzers.add(analyzer);
							} catch (Throwable e) {
								String msg = String.format("Error when starting %s!", analyzer);
								e.printStackTrace();
								m_logger.error(msg, e);
							}
						}
					}

					m_analyzers.put(hour, analyzers);
				}
			}
		}

		return analyzers;
	}

	private Map<String, Integer> getAnalyzerWeights(Map<String, MessageAnalyzer> map) {
		Map<String, Integer> weights = new HashMap<String, Integer>();
		boolean changed;

		do {
			changed = false;

			for (Map.Entry<String, MessageAnalyzer> e : map.entrySet()) {
				String[] deps = e.getValue().getDependencies();
				int weight = 0;

				for (String dep : deps) {
					Integer w = weights.get(dep);

					if (w != null) {
						if (weight <= w.intValue()) {
							weight = w.intValue() + 1;
							changed = true;
						}
					}
				}

				weights.put(e.getKey(), new Integer(weight));
			}
		} while (changed);

		return weights;
	}

	private List<String> getSortedAnalyzerNames(Map<String, MessageAnalyzer> analyzerMap, Map<String, Integer> weightMap) {
		List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(weightMap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o1.getValue().intValue() - o2.getValue().intValue();
			}
		});

		List<String> names = new ArrayList<String>();

		for (Map.Entry<String, Integer> e : list) {
			names.add(e.getKey());
		}

		return names;
	}

	@Override
	public void initialize() throws InitializationException {
		Map<String, MessageAnalyzer> map = lookupMap(MessageAnalyzer.class);
		Map<String, Integer> weights = getAnalyzerWeights(map);

		m_timeWindowManager.register(this);
		m_names = getSortedAnalyzerNames(map, weights);

		m_logger.info("Following reports configured: " + m_names);
	}

	@Override
	public void onTimeWindowEnter(int hour) {
		// do nothing here
	}

	@Override
	public void onTimeWindowExit(int hour) {
		// remove unused analyzer
		List<MessageAnalyzer> analyzers = m_analyzers.remove(hour);

		if (analyzers != null) {
			for (MessageAnalyzer analyzer : analyzers) {
				try {
					super.release(analyzer);
					analyzer.shutdown();
				} catch (Throwable e) {
					m_logger.error(String.format("Error when stopping %s!", analyzer), e);
				}
			}
		}
	}
}

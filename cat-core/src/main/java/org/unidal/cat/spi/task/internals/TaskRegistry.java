package org.unidal.cat.spi.task.internals;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.unidal.cat.spi.task.TaskConsumer;
import org.unidal.lookup.annotation.Named;

@Named
public class TaskRegistry {
	private Map<String, Set<TaskConsumer>> m_exact = new LinkedHashMap<String, Set<TaskConsumer>>();

	private Map<String, Set<TaskConsumer>> m_prefix = new LinkedHashMap<String, Set<TaskConsumer>>();

	private Map<String, Set<TaskConsumer>> m_suffix = new LinkedHashMap<String, Set<TaskConsumer>>();

	private Set<TaskConsumer> m_all = new LinkedHashSet<TaskConsumer>();

	private Map<String, Set<TaskConsumer>> m_cache = new HashMap<String, Set<TaskConsumer>>();

	private Set<TaskConsumer> findConsumers(String subject) {
		Set<TaskConsumer> consumers = new LinkedHashSet<TaskConsumer>();

		// add exactly matched
		if (m_exact.containsKey(subject)) {
			consumers.addAll(m_exact.get(subject));
		}

		// add prefixes with
		for (Map.Entry<String, Set<TaskConsumer>> e : m_prefix.entrySet()) {
			if (subject.startsWith(e.getKey())) {
				consumers.addAll(e.getValue());
			}
		}

		// add suffixes with
		for (Map.Entry<String, Set<TaskConsumer>> e : m_suffix.entrySet()) {
			if (subject.endsWith(e.getKey())) {
				consumers.addAll(e.getValue());
			}
		}

		// add "*"
		consumers.addAll(m_all);

		return consumers;
	}

	public Set<TaskConsumer> getConsumers(String subject) {
		Set<TaskConsumer> consumers = m_cache.get(subject);

		if (consumers == null) {
			consumers = findConsumers(subject);
			m_cache.put(subject, consumers);
		}

		return consumers;
	}

	private void put(Map<String, Set<TaskConsumer>> maps, String key, TaskConsumer consumer) {
		// invalidate cache
		m_cache.clear();

		Set<TaskConsumer> consumers = maps.get(key);

		if (consumers == null) {
			consumers = new LinkedHashSet<TaskConsumer>();
			maps.put(key, consumers);
		}

		consumers.add(consumer);
	}

	public void subscribe(String topic, TaskConsumer consumer) {
		put(m_exact, topic, consumer);
	}

	public void subscribePattern(String pattern, TaskConsumer consumer) {
		if (pattern.equals("*")) {
			m_all.add(consumer);
		} else if (pattern.startsWith("*.")) {
			put(m_suffix, pattern.substring(1), consumer);
		} else if (pattern.endsWith(".*")) {
			put(m_prefix, pattern.substring(0, pattern.length() - 1), consumer);
		} else {
			throw new IllegalArgumentException("Invalid pattern: " + pattern);
		}
	}
}

package org.unidal.cat.message.storage.local;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.unidal.cat.message.storage.Index;
import org.unidal.cat.message.storage.IndexManager;
import org.unidal.cat.message.storage.MessageId;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = IndexManager.class, value = "local")
public class LocalIndexManager extends ContainerHolder implements IndexManager {
	private Map<Integer, Map<String, Index>> m_indexes = new LinkedHashMap<Integer, Map<String, Index>>();

	private Map<String, Index> findOrCreateMap(Map<Integer, Map<String, Index>> map, int hour, boolean createIfNotExists) {
		Map<String, Index> m = map.get(hour);

		if (m == null && createIfNotExists) {
			synchronized (map) {
				m = map.get(hour);

				if (m == null) {
					m = new LinkedHashMap<String, Index>();
					map.put(hour, m);
				}
			}
		}

		return m;
	}

	@Override
	public Index getIndex(MessageId from, boolean createIfNotExists) throws IOException {
		int hour = from.getHour();
		String domain = from.getDomain();
		Map<String, Index> map = findOrCreateMap(m_indexes, hour, createIfNotExists);
		Index index = map == null ? null : map.get(domain);

		if (index == null && createIfNotExists) {
			synchronized (map) {
				index = map.get(domain);

				if (index == null) {
					index = lookup(Index.class, "local");
					map.put(domain, index);
				}
			}
		}

		return index;
	}
}

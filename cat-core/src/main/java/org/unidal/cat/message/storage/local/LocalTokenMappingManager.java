package org.unidal.cat.message.storage.local;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.unidal.cat.message.storage.TokenMapping;
import org.unidal.cat.message.storage.TokenMappingManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

@Named(type = TokenMappingManager.class, value = "local")
public class LocalTokenMappingManager extends ContainerHolder implements TokenMappingManager {
	private Map<Pair<Date, String>, TokenMapping> m_cache = new HashMap<Pair<Date, String>, TokenMapping>();

	@Override
	public TokenMapping getTokenMapping(Date startTime, String ip) throws IOException {
		Pair<Date, String> pair = new Pair<Date, String>(startTime, ip);
		TokenMapping mapping = m_cache.get(pair);

		if (mapping == null) {
			synchronized (this) {
				mapping = m_cache.get(pair);

				if (mapping == null) {
					mapping = lookup(TokenMapping.class, "local");
					mapping.open(startTime, ip);
					m_cache.put(pair, mapping);
				}
			}
		}

		return mapping;
	}
}

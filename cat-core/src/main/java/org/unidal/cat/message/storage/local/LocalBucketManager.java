package org.unidal.cat.message.storage.local;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.metric.Benchmark;
import org.unidal.cat.metric.BenchmarkManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = BucketManager.class, value = "local")
public class LocalBucketManager extends ContainerHolder implements BucketManager {
	@Inject
	private BenchmarkManager m_benchmarkManager;

	private Map<Integer, Map<String, Bucket>> m_buckets = new LinkedHashMap<Integer, Map<String, Bucket>>();

	@Override
	public void closeBuckets() {
		for (Map<String, Bucket> map : m_buckets.values()) {
			for (Bucket bucket : map.values()) {
				bucket.close();
			}
		}

		m_buckets.clear();
	}

	private Map<String, Bucket> findOrCreateMap(Map<Integer, Map<String, Bucket>> map, int hour,
	      boolean createIfNotExists) {
		Map<String, Bucket> m = map.get(hour);

		if (m == null && createIfNotExists) {
			synchronized (map) {
				m = map.get(hour);

				if (m == null) {
					m = new LinkedHashMap<String, Bucket>();
					map.put(hour, m);
				}
			}
		}

		return m;
	}

	@Override
	public Bucket getBucket(String domain, int hour, boolean createIfNotExists) throws IOException {
		Map<String, Bucket> map = findOrCreateMap(m_buckets, hour, createIfNotExists);
		Bucket bucket = map == null ? null : map.get(domain);

		if (bucket == null && createIfNotExists) {
			synchronized (map) {
				bucket = map.get(domain);

				if (bucket == null) {
					Benchmark benchmark = m_benchmarkManager.get(domain + ":" + hour);

					bucket = lookup(Bucket.class, "local");
					bucket.setBenchmark(benchmark);
					map.put(domain, bucket);
				}
			}
		}

		return bucket;
	}
}

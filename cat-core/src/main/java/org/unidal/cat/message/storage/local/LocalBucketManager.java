package org.unidal.cat.message.storage.local;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.metric.Benchmark;
import org.unidal.cat.metric.BenchmarkManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.helper.TimeHelper;

@Named(type = BucketManager.class, value = "local")
public class LocalBucketManager extends ContainerHolder implements BucketManager, LogEnabled {
	@Inject
	private BenchmarkManager m_benchmarkManager;

	private Map<Integer, Map<String, Bucket>> m_buckets = new LinkedHashMap<Integer, Map<String, Bucket>>();

	private Logger m_logger;

	@Override
	public void closeBuckets(long timestamp) {
		int hour = (int) (timestamp / TimeHelper.ONE_HOUR);
		Map<String, Bucket> map = m_buckets.get(hour);

		if (map != null) {
			for (Bucket bucket : map.values()) {
				bucket.close();
				m_logger.info("close bucket " + bucket.toString());
				super.release(bucket);
			}
		}
		m_buckets.remove(hour);
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
	public Bucket getBucket(String domain, String ip, int hour, boolean createIfNotExists) throws IOException {
		Map<String, Bucket> map = findOrCreateMap(m_buckets, hour, createIfNotExists);
		Bucket bucket = map == null ? null : map.get(domain);

		if (bucket == null && createIfNotExists) {
			synchronized (map) {
				bucket = map.get(domain);

				if (bucket == null) {
					Benchmark benchmark = m_benchmarkManager.get(domain + ":" + hour);

					bucket = lookup(Bucket.class, "local");
					bucket.setBenchmark(benchmark);
					bucket.initialize(domain, ip, hour);
					map.put(domain, bucket);
				}
			}
		}

		return bucket;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}

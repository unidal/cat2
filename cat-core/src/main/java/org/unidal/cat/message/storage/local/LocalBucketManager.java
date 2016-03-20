package org.unidal.cat.message.storage.local;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.FileBuilder;
import org.unidal.cat.message.storage.FileBuilder.FileType;
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

	@Inject("local")
	private FileBuilder m_bulider;

	private Map<Integer, Map<String, Bucket>> m_buckets = new LinkedHashMap<Integer, Map<String, Bucket>>();

	private Logger m_logger;

	@Override
	public void closeBuckets(long timestamp) {
		Set<Integer> removed = new HashSet<Integer>();

		for (Entry<Integer, Map<String, Bucket>> entry : m_buckets.entrySet()) {
			Integer hour = entry.getKey();
			long time = hour * TimeHelper.ONE_HOUR;

			if (time <= timestamp) {
				removed.add(hour);
			}
		}

		for (Integer i : removed) {
			Map<String, Bucket> buckets = m_buckets.remove(i);

			for (Bucket bucket : buckets.values()) {
				bucket.close();
				super.release(bucket);
				m_logger.info("close bucket " + bucket.toString());
			}
		}
	}

	private Map<String, Bucket> findOrCreateMap(Map<Integer, Map<String, Bucket>> map, int hour) {
		Map<String, Bucket> m = map.get(hour);

		if (m == null) {
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

	private boolean bucketExsit(String domain, String ip, int hour) {
		long timestamp = hour * 3600 * 1000L;
		Date startTime = new Date(timestamp);
		File dataPath = m_bulider.getFile(domain, startTime, ip, FileType.DATA);
		File indexPath = m_bulider.getFile(domain, startTime, ip, FileType.INDEX);

		return dataPath.exists() && indexPath.exists();
	}

	@Override
	public Bucket getBucket(String domain, String ip, int hour, boolean createIfNotExists) throws IOException {
		Map<String, Bucket> map = findOrCreateMap(m_buckets, hour);
		Bucket bucket = map.get(domain);

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
		} else if (createIfNotExists == false) {
			if (bucketExsit(domain, ip, hour)) {
				synchronized (map) {
					bucket = map.get(domain);
					if (bucket == null) {
						bucket = lookup(Bucket.class, "local");
						bucket.initialize(domain, ip, hour);
						map.put(domain, bucket);
					}
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

package org.unidal.cat.message.storage.internals;

import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.cat.message.storage.Block;
import org.unidal.cat.message.storage.BlockWriter;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.metric.Benchmark;
import org.unidal.cat.metric.BenchmarkEnabled;
import org.unidal.cat.metric.BenchmarkManager;
import org.unidal.cat.metric.Metric;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.Transaction;

@Named(type = BlockWriter.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultBlockWriter implements BlockWriter {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private BenchmarkManager m_benchmarkManager;

	private int m_index;

	private BlockingQueue<Block> m_queue;

	private long m_hour;

	private int m_count;

	private AtomicBoolean m_enabled;

	private CountDownLatch m_latch;

	@Override
	public String getName() {
		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(TimeUnit.HOURS.toMillis(m_hour));
		return getClass().getSimpleName() + "-" + cal.get(Calendar.HOUR_OF_DAY) + "-" + m_index;
	}

	@Override
	public void initialize(int hour, int index, BlockingQueue<Block> queue) {
		m_hour = hour;
		m_index = index;
		m_queue = queue;
		m_enabled = new AtomicBoolean(true);
		m_latch = new CountDownLatch(1);
	}

	@Override
	public void run() {
		String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
		Benchmark benchmark = m_benchmarkManager.get("BlockWriter-" + m_index);
		Metric metric = benchmark.get("wait");
		Block block;

		try {
			while (m_enabled.get() || !m_queue.isEmpty()) {
				metric.start();
				block = m_queue.poll(5, TimeUnit.MILLISECONDS);
				metric.end();

				if (block != null) {
					try {
						Bucket bucket = m_bucketManager.getBucket(block.getDomain(), ip, block.getHour(), true);

						if (bucket instanceof BenchmarkEnabled) {
							((BenchmarkEnabled) bucket).setBenchmark(benchmark);
						}

						if ((++m_count) % 100 == 0) {
							Transaction t = Cat.newTransaction("Block", block.getDomain());

							bucket.puts(block.getData(), block.getMappings());
							t.setStatus(Transaction.SUCCESS);
							t.complete();
						} else {
							bucket.puts(block.getData(), block.getMappings());
						}
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		} catch (InterruptedException e) {
			// ignore it
		}

		m_latch.countDown();

		System.out.println(getClass().getSimpleName() + "-" + m_index + " is shutdown");
		benchmark.print();
	}

	@Override
	public void shutdown() {
		m_enabled.set(false);

		try {
			m_latch.await();
		} catch (InterruptedException e) {
			// ignore it
		}
	}
}

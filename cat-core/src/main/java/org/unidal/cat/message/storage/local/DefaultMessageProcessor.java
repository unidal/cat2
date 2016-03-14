package org.unidal.cat.message.storage.local;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.cat.message.MessageId;
import org.unidal.cat.message.storage.Block;
import org.unidal.cat.message.storage.BlockDumper;
import org.unidal.cat.message.storage.MessageProcessor;
import org.unidal.cat.message.storage.internals.DefaultBlock;
import org.unidal.cat.metric.Benchmark;
import org.unidal.cat.metric.BenchmarkManager;
import org.unidal.cat.metric.Metric;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageProcessor.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultMessageProcessor implements MessageProcessor {
	@Inject
	private BlockDumper m_dumper;

	@Inject
	private BenchmarkManager m_benchmarkManager;

	private int m_index;

	private BlockingQueue<MessageTree> m_queue;

	private ConcurrentHashMap<String, Block> m_blocks = new ConcurrentHashMap<String, Block>();

	private AtomicBoolean m_enabled;

	@Override
	public ByteBuf findTree(MessageId id) {
		String domain = id.getDomain();
		Block block = m_blocks.get(domain);

		if (block != null) {
			return block.findTree(id);
		}
		
		return null;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName() + "-" + m_index;
	}

	@Override
	public void initialize(int index, BlockingQueue<MessageTree> queue) {
		m_index = index;
		m_queue = queue;
		m_enabled = new AtomicBoolean(true);
	}

	@Override
	public void run() {
		Benchmark benchmark = m_benchmarkManager.get("MessageProcessor-" + m_index);
		Metric wm = benchmark.get("wait");
		Metric pm = benchmark.get("pack");
		MessageTree tree;

		try {
			while (m_enabled.get()) {
				wm.start();
				tree = m_queue.poll(5, TimeUnit.MILLISECONDS);
				wm.end();

				if (tree != null) {
					MessageId id = MessageId.parse(tree.getMessageId());
					String domain = id.getDomain();
					int hour = id.getHour();
					Block block = m_blocks.get(domain);

					if (block == null) {
						block = new DefaultBlock(domain, hour);
						m_blocks.put(domain, block);
					}

					try {
						pm.start();

						if (block.isFull()) {
							block.finish();

							m_dumper.dump(block);
							block = new DefaultBlock(domain, hour);
							m_blocks.put(domain, block);
						}
						
						block.pack(id, tree.getBuffer());
						pm.end();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (InterruptedException e) {
			// ignore it
		}

		System.out.println(getClass().getSimpleName() + "-" + m_index + " is shutdown");
		benchmark.print();
	}
	

	@Override
	public void shutdown() {
		m_enabled.set(false);

		for (Block block : m_blocks.values()) {
			try {
				block.finish();

				m_dumper.dump(block);
			} catch (IOException e) {
				// ignore it
			}
		}

		m_blocks.clear();
	}
}

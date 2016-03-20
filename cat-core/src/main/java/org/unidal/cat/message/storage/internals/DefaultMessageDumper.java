package org.unidal.cat.message.storage.internals;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.unidal.cat.message.MessageId;
import org.unidal.cat.message.QueueFullException;
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.MessageDumper;
import org.unidal.cat.message.storage.MessageProcessor;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageDumper.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultMessageDumper extends ContainerHolder implements MessageDumper {
	@Inject
	private BlockDumperManager m_blockDumperManager;

	@Inject
	private BucketManager m_bucketManager;

	private List<BlockingQueue<MessageTree>> m_queues = new ArrayList<BlockingQueue<MessageTree>>();

	private List<MessageProcessor> m_processors = new ArrayList<MessageProcessor>();

	private int m_failCount = -1;

	@Override
	public void awaitTermination(int hour) throws InterruptedException {
		while (true) {
			boolean allEmpty = true;

			for (BlockingQueue<MessageTree> queue : m_queues) {
				if (!queue.isEmpty()) {
					allEmpty = false;
					break;
				}
			}

			if (allEmpty) {
				break;
			} else {
				TimeUnit.MILLISECONDS.sleep(1);
			}
		}

		for (MessageProcessor processor : m_processors) {
			processor.shutdown();
		}

		m_blockDumperManager.closeDumper(hour);
		m_bucketManager.closeBuckets(hour);
	}

	@Override
	public ByteBuf find(MessageId id) {
		int index = getIndex(id.getDomain());
		MessageProcessor process = m_processors.get(index);
		ByteBuf tree = process.findTree(id);

		if (tree == null) {
			process = m_processors.get(m_processors.size() - 1); // last one

			tree = process.findTree(id);
		}

		return tree;
	}

	private int getIndex(String domain) {
		int hash = Math.abs(domain.hashCode());
		int index = hash % (m_processors.size() - 1); // last one for message overflow

		return index;
	}

	public void initialize(int hour) {
		for (int i = 0; i < 10; i++) {
			BlockingQueue<MessageTree> queue = new LinkedBlockingQueue<MessageTree>(10000);
			MessageProcessor processor = lookup(MessageProcessor.class);

			m_queues.add(queue);
			m_processors.add(processor);

			processor.initialize(hour, i, queue);
			Threads.forGroup("Cat").start(processor);
		}
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		int index = getIndex(domain);
		BlockingQueue<MessageTree> queue = m_queues.get(index);

		if (!queue.offer(tree)) { // overflow
			BlockingQueue<MessageTree> last = m_queues.get(m_queues.size() - 1);
			boolean success = last.offer(tree);

			if (!success && (++m_failCount % 100) == 0) {
				Cat.logError(new QueueFullException("Error when adding message to queue, fails: " + m_failCount));
			}
		}
	}
}

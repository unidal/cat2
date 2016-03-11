package org.unidal.cat.message.storage.local;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.message.storage.BlockDumper;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.MessageDumper;
import org.unidal.cat.message.storage.MessageProcessor;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageDumper.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultMessageDumper extends ContainerHolder implements MessageDumper, Initializable {
	private List<BlockingQueue<MessageTree>> m_queues = new ArrayList<BlockingQueue<MessageTree>>();

	private List<MessageProcessor> m_processors = new ArrayList<MessageProcessor>();

	private int m_failCount = -1;

	@Override
	public void awaitTermination() throws InterruptedException {
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

		BlockDumper dumper = lookup(BlockDumper.class);

		dumper.awaitTermination();

		BucketManager manager = lookup(BucketManager.class, "local");

		manager.closeBuckets();
	}

	@Override
	public void initialize() throws InitializationException {
		for (int i = 0; i < 10; i++) {
			BlockingQueue<MessageTree> queue = new LinkedBlockingQueue<MessageTree>(10000);
			MessageProcessor processor = lookup(MessageProcessor.class);

			m_queues.add(queue);
			m_processors.add(processor);

			processor.initialize(i, queue);
			Threads.forGroup("Cat").start(processor);
		}
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		int hash = Math.abs(domain.hashCode());
		int index = hash % (m_processors.size() - 1); // last one for message overflow
		BlockingQueue<MessageTree> queue = m_queues.get(index);

		if (!queue.offer(tree)) { // overflow
			BlockingQueue<MessageTree> last = m_queues.get(m_queues.size() - 1);
			boolean success = last.offer(tree);

			if (!success && (++m_failCount % 100) == 0) {
				Cat.logError(new RuntimeException("Error when adding message to queue, fails: " + m_failCount));
			}
		}
	}
}

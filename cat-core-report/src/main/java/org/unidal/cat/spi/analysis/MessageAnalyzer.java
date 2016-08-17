package org.unidal.cat.spi.analysis;

import java.util.Map;

import org.unidal.helper.Threads.Task;

import com.dianping.cat.message.spi.MessageTree;

/**
 * Message analyzer is responsible of processing each message in the queue, and producing a report normally.
 * <p>
 *
 * For each hour, there is one instance will be instantiated and be assigned with a specific queue.
 */
public interface MessageAnalyzer extends Task {
	public boolean handle(MessageTree tree);

	public void configure(Map<String, String> properties);

	public void doCheckpoint(boolean atEnd) throws Exception;

	public String[] getDependencies();

	public void initialize(int index, int hour) throws Exception;

	public void destroy();
}

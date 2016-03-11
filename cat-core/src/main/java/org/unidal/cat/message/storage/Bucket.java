package org.unidal.cat.message.storage;

import java.io.IOException;

import org.unidal.cat.message.MessageId;
import org.unidal.cat.metric.BenchmarkEnabled;

public interface Bucket extends BenchmarkEnabled {
	public void close();

	public Block get(MessageId id) throws IOException;

	public void put(Block block) throws IOException;
}

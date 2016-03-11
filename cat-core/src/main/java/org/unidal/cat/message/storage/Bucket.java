package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Map;

import org.unidal.cat.message.MessageId;
import org.unidal.cat.metric.BenchmarkEnabled;

public interface Bucket extends BenchmarkEnabled {
	public void close();

	public ByteBuf get(MessageId id) throws IOException;

	public void initialize(String domain, String ip, int hour) throws IOException;

	public void puts(ByteBuf buf, Map<MessageId, Integer> mappings) throws IOException;
}

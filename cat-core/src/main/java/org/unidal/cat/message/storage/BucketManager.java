package org.unidal.cat.message.storage;

import java.io.IOException;

public interface BucketManager {
	public void closeBuckets(long timestamp);

	public Bucket getBucket(String domain, String ip, int hour, boolean createIfNotExists) throws IOException;
}

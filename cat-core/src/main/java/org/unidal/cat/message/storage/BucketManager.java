package org.unidal.cat.message.storage;

import java.io.IOException;

public interface BucketManager {
	public Bucket getBucket(String domain, int hour, boolean createIfNotExists) throws IOException;
}

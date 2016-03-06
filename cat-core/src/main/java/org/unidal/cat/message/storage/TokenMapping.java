package org.unidal.cat.message.storage;

import java.io.IOException;
import java.util.Date;

public interface TokenMapping {
	public void close();

	public long getLastAccessTime();

	public String lookup(int index) throws IOException;

	public int map(String token) throws IOException;

	public void open(Date startTime, String ip) throws IOException;
}

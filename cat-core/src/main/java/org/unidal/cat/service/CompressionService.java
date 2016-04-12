package org.unidal.cat.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface CompressionService {
	public OutputStream compress(OutputStream out) throws IOException;

	public InputStream decompress(InputStream in) throws IOException;
}

package org.unidal.cat.service.internals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.unidal.cat.service.CompressionService;
import org.unidal.lookup.annotation.Named;

@Named(type = CompressionService.class, value = GzipCompressionService.ID)
public class GzipCompressionService implements CompressionService {
	public static final String ID = "gzip";

	@Override
	public OutputStream compress(OutputStream out) throws IOException {
		return new GZIPOutputStream(out);
	}

	@Override
	public InputStream decompress(InputStream in) throws IOException {
		return new GZIPInputStream(in);
	}
}

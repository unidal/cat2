package org.unidal.cat.service.internals;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.unidal.cat.service.CompressionService;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = CompressionService.class)
public class DefaultCompressionService implements CompressionService {
	@Inject(ZlibCompressionService.ID)
	private CompressionService m_zlib;

	@Inject(GzipCompressionService.ID)
	private CompressionService m_gzip;

	@Override
	public OutputStream compress(OutputStream out) throws IOException {
		return m_zlib.compress(out);
	}

	@Override
	public InputStream decompress(InputStream in) throws IOException {
		BufferedInputStream buf = new BufferedInputStream(in);

		buf.mark(2);
		int b1 = buf.read() & 0xFF;
		int b2 = buf.read() & 0xFF;

		buf.reset();

		if (b1 == 0x1F && b2 == 0x8B) { // 0x8b1f
			// gzip format
			in = m_gzip.decompress(buf);
		} else {
			// or zlib format
			in = m_zlib.decompress(buf);
		}

		return in;
	}
}

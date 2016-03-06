package org.unidal.cat.service.internals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.unidal.cat.service.CompressionService;
import org.unidal.lookup.annotation.Named;

@Named(type = CompressionService.class, value = ZlibCompressionService.ID)
public class ZlibCompressionService implements CompressionService {
	public static final String ID = "zlib";

	private int m_level = 5;

	private int m_bufferSize = 512;

	@Override
	public OutputStream compress(OutputStream out) throws IOException {
		Deflater deflater = new Deflater(m_level, true);

		return new DeflaterOutputStream(out, deflater, m_bufferSize);
	}

	@Override
	public InputStream decompress(InputStream in) throws IOException {
		Inflater inflater = new Inflater(true);

		return new InflaterInputStream(in, inflater, m_bufferSize);
	}
}

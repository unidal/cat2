package org.unidal.cat.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.service.internals.GzipCompressionService;
import org.unidal.cat.service.internals.ZlibCompressionService;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

public class CompressionServiceTest extends ComponentTestCase {
	private static final String TEXT = "Hello, World!";

	@Test
	public void testDefault() throws IOException {
		CompressionService cs = lookup(CompressionService.class);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputStream cout = cs.compress(out);

		cout.write(TEXT.getBytes());
		cout.close();

		InputStream in = cs.decompress(new ByteArrayInputStream(out.toByteArray()));
		String actual = Files.forIO().readFrom(in, "utf-8");

		Assert.assertEquals(TEXT, actual);
	}

	@Test
	public void testGzip() throws IOException {
		CompressionService cs = lookup(CompressionService.class);
		CompressionService gzip = lookup(CompressionService.class, GzipCompressionService.ID);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputStream cout = gzip.compress(out);

		cout.write(TEXT.getBytes());
		cout.close();

		InputStream in = cs.decompress(new ByteArrayInputStream(out.toByteArray()));
		String actual = Files.forIO().readFrom(in, "utf-8");

		Assert.assertEquals(TEXT, actual);
	}

	@Test
	public void testZlib() throws IOException {
		CompressionService cs = lookup(CompressionService.class);
		CompressionService deflate = lookup(CompressionService.class, ZlibCompressionService.ID);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputStream cout = deflate.compress(out);

		cout.write(TEXT.getBytes());
		cout.close();

		InputStream in = cs.decompress(new ByteArrayInputStream(out.toByteArray()));
		String actual = Files.forIO().readFrom(in, "utf-8");

		Assert.assertEquals(TEXT, actual);
	}
}

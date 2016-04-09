package org.unidal.cat.spi.report;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportPeriod;

public interface ReportDelegate<T extends Report> {
	public T aggregate(ReportPeriod period, Collection<T> reports);

	public String buildXml(T report);

	public String getName();

	public T parseXml(String xml);

	public T readStream(InputStream in) throws IOException;

	public void writeStream(OutputStream out, T report) throws IOException;

	public T createLocal(ReportPeriod period, String domain, Date startTime);
}

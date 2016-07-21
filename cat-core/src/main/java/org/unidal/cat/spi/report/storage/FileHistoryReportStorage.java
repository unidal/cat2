package org.unidal.cat.spi.report.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.cat.service.CompressionService;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportConfiguration;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.ReportStoragePolicy;
import org.unidal.cat.spi.report.ReportDelegate;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportStorage.class, value = FileHistoryReportStorage.ID)
public class FileHistoryReportStorage<T extends Report> implements ReportStorage<T> {
	public static final String ID = "file-history";

	@Inject
	private ReportConfiguration m_configuration;

	@Inject
	private CompressionService m_compression;

	File getReportFile(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain) {
		MessageFormat format = new MessageFormat("report2/{0,date,yyyy}-{0,date,MM}/{0,date,dd}/{1}/{2}" //
		      + "/{3}.rpt");
		String report = delegate.getName();
		String path = format.format(new Object[] { startTime, report, period.getName(), domain });
		File file = new File(m_configuration.getBaseDataDir(), path);

		return file;
	}

	@Override
	public List<T> loadAll(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain)
	      throws IOException {
		List<T> reports = new ArrayList<T>(1);
		File file = getReportFile(delegate, period, startTime, domain);

		loadReport(reports, delegate, file);

		return reports;
	}

	@Override
	public List<T> loadAllByDateRange(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, Date endTime,
	      String domain) throws IOException {
		List<T> reports = new ArrayList<T>();
		Date time = startTime;

		while (time.before(endTime)) {
			File file = getReportFile(delegate, period, startTime, domain);

			loadReport(reports, delegate, file);
			time = period.getNextStartTime(time);
		}

		return reports;
	}

	private void loadReport(List<T> reports, ReportDelegate<T> delegate, File file) throws FileNotFoundException,
	      IOException {
		if (file.exists() && file.canRead()) {
			InputStream in = new FileInputStream(file);
			InputStream cin = m_compression.decompress(in);

			try {
				T report = delegate.readStream(cin);

				reports.add(report);
			} finally {
				try {
					cin.close();
				} catch (IOException e) {
					// ignore it
				}
			}
		}
	}

	private void saveReport(ReportDelegate<T> delegate, File file, T report) throws FileNotFoundException, IOException {
		file.getParentFile().mkdirs();

		OutputStream out = new FileOutputStream(file);
		OutputStream cout = m_compression.compress(out);

		try {
			delegate.writeStream(cout, report);
		} finally {
			try {
				cout.close();
			} catch (IOException e) {
				// ignore it
			}
		}
	}

	@Override
	public void store(ReportDelegate<T> delegate, ReportPeriod period, T report, int index, ReportStoragePolicy policy)
	      throws IOException {
		File file = getReportFile(delegate, period, report.getStartTime(), report.getDomain());

		saveReport(delegate, file, report);
	}
}

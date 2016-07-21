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
import org.unidal.helper.Scanners;
import org.unidal.helper.Scanners.FileMatcher;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportStorage.class, value = FileHourlyReportStorage.ID)
public class FileHourlyReportStorage<T extends Report> implements ReportStorage<T> {
	public static final String ID = "file-hourly";

	@Inject
	private ReportConfiguration m_configuration;

	@Inject
	private CompressionService m_compression;

	File getReportFile(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain, int index) {
		MessageFormat format = new MessageFormat("report2/{0,date,yyyy}-{0,date,MM}/{0,date,dd}/{1}/{2}/{0,date,HH}"
		      + "/{3}-{4}.rpt");
		String report = delegate.getName();
		String path = format.format(new Object[] { startTime, report, period.getName(), domain, index });
		File file = new File(m_configuration.getBaseDataDir(), path);

		return file;
	}

	List<File> getReportFiles(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, final String domain) {
		MessageFormat format = new MessageFormat("report2/{0,date,yyyy}-{0,date,MM}/{0,date,dd}/{1}/{2}/{0,date,HH}");
		String report = delegate.getName();
		String path = format.format(new Object[] { startTime, report, period.getName() });
		File baseDir = new File(m_configuration.getBaseDataDir(), path);
		List<File> files = Scanners.forDir().scan(baseDir, new FileMatcher() {
			@Override
			public Direction matches(File base, String path) {
				int pos = path.lastIndexOf('-');

				if (pos > 0) {
					if (domain == null || domain.equals(path.substring(0, pos))) {
						return Direction.MATCHED;
					}
				}

				return Direction.NEXT;
			}
		});

		return files;
	}

	@Override
	public List<T> loadAll(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain)
	      throws IOException {
		List<T> reports = new ArrayList<T>();
		List<File> files = getReportFiles(delegate, period, startTime, domain);

		for (File file : files) {
			T report = loadReport(delegate, file);

			if (report != null) {
				reports.add(report);
			}
		}

		return reports;
	}

	@Override
	public List<T> loadAllByDateRange(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, Date endTime,
	      String domain) throws IOException {
		List<T> reports = new ArrayList<T>();
		Date time = startTime;

		while (time.before(endTime)) {
			List<File> files = getReportFiles(delegate, period, startTime, domain);

			for (File file : files) {
				T report = loadReport(delegate, file);

				if (report != null) {
					reports.add(report);
				}
			}

			time = period.getNextStartTime(time);
		}

		return reports;
	}

	private T loadReport(ReportDelegate<T> delegate, File file) throws FileNotFoundException, IOException {
		if (file.exists() && file.canRead()) {
			InputStream in = new FileInputStream(file);
			InputStream cin = m_compression.decompress(in);

			try {
				T report = delegate.readStream(cin);

				return report;
			} finally {
				try {
					cin.close();
				} catch (IOException e) {
					// ignore it
				}
			}
		}

		return null;
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
		File file = getReportFile(delegate, period, report.getStartTime(), report.getDomain(), index);

		saveReport(delegate, file, report);
	}
}

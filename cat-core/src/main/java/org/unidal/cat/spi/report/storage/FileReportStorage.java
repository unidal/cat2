package org.unidal.cat.spi.report.storage;

import java.io.File;
import java.io.FileInputStream;
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

@Named(type = ReportStorage.class, value = FileReportStorage.ID)
public class FileReportStorage<T extends Report> implements ReportStorage<T> {
	public static final String ID = "file";

	@Inject
	private ReportConfiguration m_configuration;

	@Inject
	private CompressionService m_compression;

	File getDailyReportFile(ReportDelegate<T> delegate, Date startTime, String domain) {
		MessageFormat format = new MessageFormat("report/{0,date,yyyy}-{0,date,MM}/{0,date,dd}/{1}/daily/{2}.rpt");
		String type = delegate.getName();
		String path = format.format(new Object[] { startTime, type, domain });
		File file = new File(m_configuration.getBaseDataDir(), path);

		return file;
	}

	List<File> getDailyReportFilesInHourly(ReportDelegate<T> delegate, Date startTime, final String domain) {
		MessageFormat format = new MessageFormat("report/{0,date,yyyy}-{0,date,MM}/{0,date,dd}/{1}");
		String type = delegate.getName();
		String path = format.format(new Object[] { startTime, type });
		File baseDir = new File(m_configuration.getBaseDataDir(), path);
		List<File> files = Scanners.forDir().scan(baseDir, new FileMatcher() {
			@Override
			public Direction matches(File base, String path) {
				int pos1 = path.lastIndexOf('-');
				int pos2 = path.lastIndexOf('/');

				if (pos1 > pos2 && pos2 > 0) {
					if (domain == null || domain.equals(path.substring(pos2 + 1, pos1))) {
						return Direction.MATCHED;
					}
				}

				return Direction.DOWN;
			}
		});

		return files;
	}

	File getHourlyReportFile(ReportDelegate<T> delegate, Date startTime, String domain, int index) {
		MessageFormat format = new MessageFormat(
		      "report/{0,date,yyyy}-{0,date,MM}/{0,date,dd}/{1}/{0,date,HH}/{2}-{3}.rpt");
		String type = delegate.getName();
		String path = format.format(new Object[] { startTime, type, domain, index });
		File file = new File(m_configuration.getBaseDataDir(), path);

		return file;
	}

	List<File> getHourlyReportFiles(ReportDelegate<T> delegate, Date startTime, final String domain) {
		MessageFormat format = new MessageFormat("report/{0,date,yyyy}-{0,date,MM}/{0,date,dd}/{1}/{0,date,HH}");
		String type = delegate.getName();
		String path = format.format(new Object[] { startTime, type });
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
		switch (period) {
		case HOUR:
			return loadAllHourly(delegate, period, startTime, domain);
		case DAY:
			return loadAllDaily(delegate, period, startTime, domain);
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	@Override
	public List<T> loadAllByDateRange(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, Date endTime,
	      String domain) throws IOException {
		throw new UnsupportedOperationException("Not implemented yet!");// TODO
	}

	private List<T> loadAllDaily(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain)
	      throws IOException {
		List<T> reports = new ArrayList<T>(1);
		File file = getDailyReportFile(delegate, startTime, domain);

		if (file.exists()) {
			// try to find from aggregated daily report file
			loadFromFile(reports, delegate, file);
		} else {
			// aggregate them and save it
			List<T> hourlyReports = new ArrayList<T>(2);
			List<File> hourlyFiles = getDailyReportFilesInHourly(delegate, startTime, domain);

			for (File hourlyFile : hourlyFiles) {
				loadFromFile(hourlyReports, delegate, hourlyFile);
			}

			hourlyReports.add(delegate.createLocal(ReportPeriod.DAY, domain, startTime));

			T report = delegate.aggregate(period, hourlyReports);

			reports.add(report);
		}

		return reports;
	}

	private List<T> loadAllHourly(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain)
	      throws IOException {
		List<File> files = getHourlyReportFiles(delegate, startTime, domain);
		List<T> reports = new ArrayList<T>();

		for (File file : files) {
			loadFromFile(reports, delegate, file);
		}

		return reports;
	}

	private void loadFromFile(List<T> reports, ReportDelegate<T> delegate, File file) throws IOException {
		if (file.exists()) {
			InputStream in = new FileInputStream(file);
			InputStream cin = m_compression.decompress(in);
			T report = delegate.readStream(cin);

			if (reports != null) {
				reports.add(report);
			}
		}
	}

	private void saveToFile(ReportDelegate<T> delegate, File file, T report) throws IOException {
		file.getParentFile().mkdirs();

		OutputStream out = new FileOutputStream(file);
		OutputStream cout = m_compression.compress(out);

		delegate.writeStream(cout, report);
		cout.close();
	}

	@Override
	public void store(ReportDelegate<T> delegate, ReportPeriod period, T report, int index, ReportStoragePolicy policy)
	      throws IOException {
		if (!policy.forFile()) {
			return;
		}

		switch (period) {
		case HOUR:
			File hourly = getHourlyReportFile(delegate, report.getStartTime(), report.getDomain(), index);

			saveToFile(delegate, hourly, report);
			break;
		case DAY:
			File daily = getDailyReportFile(delegate, report.getStartTime(), report.getDomain());

			saveToFile(delegate, daily, report);
			break;
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}
}

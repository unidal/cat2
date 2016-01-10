package org.unidal.cat.report.internals;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportConfiguration;
import org.unidal.cat.report.ReportPeriod;
import org.unidal.cat.report.spi.ReportDelegate;
import org.unidal.helper.Files;
import org.unidal.helper.Scanners;
import org.unidal.helper.Scanners.FileMatcher;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportStorage.class, value = FileReportStorage.ID)
public class FileReportStorage<T extends Report> implements ReportStorage<T> {
	public static final String ID = "file";

	@Inject
	private ReportConfiguration m_configuration;

	File getDailyReportFile(ReportDelegate<T> delegate, Date startTime, String domain) {
		MessageFormat format = new MessageFormat("report/{0,date,yyyy}-{0,date,MM}/{0,date,dd}/{1}/daily/{2}.xml");
		String type = delegate.getName();
		String path = format.format(new Object[] { startTime, type, domain });
		File file = new File(m_configuration.getBaseDataDir(), path);

		return file;
	}

	File getHourlyReportFile(ReportDelegate<T> delegate, Date startTime, String domain, int index) {
		MessageFormat format = new MessageFormat(
		      "report/{0,date,yyyy}-{0,date,MM}/{0,date,dd}/{1}/{0,date,HH}/{2}-{3}.xml");
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

	private List<T> loadAllDaily(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain)
	      throws IOException {
		List<T> reports = new ArrayList<T>(1);
		File file = getDailyReportFile(delegate, startTime, domain);

		if (file.exists()) {
			// try to find from aggregated daily report file
			load(reports, delegate, file);
		} else {
			// aggregate them and save it
			List<T> hourlyReports = new ArrayList<T>(2);
			List<File> hourlyFiles = getDailyReportFilesInHourly(delegate, startTime, domain);

			for (File hourlyFile : hourlyFiles) {
				load(hourlyReports, delegate, hourlyFile);
			}

			hourlyReports.add(delegate.createLocal(ReportPeriod.DAY, domain, startTime));

			T report = delegate.aggregate(period, hourlyReports);

			reports.add(report);
		}

		return reports;
	}

	private T load(List<T> reports, ReportDelegate<T> delegate, File file) throws IOException {
		if (file.exists()) {
			String xml = Files.forIO().readFrom(file, "utf-8");

			if (xml != null && xml.length() > 0) {
				T report = delegate.parseXml(xml);

				if (reports != null) {
					reports.add(report);
				}

				return report;
			}
		}

		return null;
	}

	private List<T> loadAllHourly(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain)
	      throws IOException {
		List<File> files = getHourlyReportFiles(delegate, startTime, domain);
		List<T> reports = new ArrayList<T>();

		for (File file : files) {
			load(reports, delegate, file);
		}

		return reports;
	}

	@Override
	public void store(ReportDelegate<T> delegate, ReportPeriod period, T report, int index, ReportStoragePolicy policy)
	      throws IOException {
		if (!policy.forFile()) {
			return;
		}

		switch (period) {
		case HOUR:
			storeHourlyReport(delegate, report, index);
			break;
		case DAY:
			storeDailyReport(delegate, report);
			break;
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	private void storeDailyReport(ReportDelegate<T> delegate, T report) throws IOException {
		File file = getDailyReportFile(delegate, report.getStartTime(), report.getDomain());
		String xml = delegate.buildXml(report);

		Files.forIO().writeTo(file, xml);
	}

	private void storeHourlyReport(ReportDelegate<T> delegate, T report, int index) throws IOException {
		File file = getHourlyReportFile(delegate, report.getStartTime(), report.getDomain(), index);
		String xml = delegate.buildXml(report);

		Files.forIO().writeTo(file, xml);
	}
}

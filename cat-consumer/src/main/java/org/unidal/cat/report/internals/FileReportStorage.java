package org.unidal.cat.report.internals;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportPeriod;
import org.unidal.cat.report.spi.ReportDelegate;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportStorage.class, value = FileReportStorage.ID)
public class FileReportStorage<T extends Report> implements ReportStorage<T> {
	public static final String ID = "file";

	private File m_baseDir = new File("target");

	File getDailyReportFile(ReportDelegate<T> delegate, Date startTime, String domain) {
		MessageFormat format = new MessageFormat("report/{0,date,yyyy}-{0,date,MM}/{0,date,dd}/daily/{1}/{2}.xml");
		String type = delegate.getName();
		String path = format.format(new Object[] { startTime, type, domain });
		File file = new File(m_baseDir, path);

		return file;
	}

	File getHourlyReportFile(ReportDelegate<T> delegate, Date startTime, String domain) {
		MessageFormat format = new MessageFormat("report/{0,date,yyyy}-{0,date,MM}/{0,date,dd}/{0,date,HH}/{1}/{2}.xml");
		String type = delegate.getName();
		String path = format.format(new Object[] { startTime, type, domain });
		File file = new File(m_baseDir, path);

		return file;
	}

	File[] getHourlyReportFiles(ReportDelegate<T> delegate, Date startTime) {
		MessageFormat format = new MessageFormat("report/{0,date,yyyy}-{0,date,MM}/{0,date,dd}/{0,date,HH}/{1}");
		String type = delegate.getName();
		String path = format.format(new Object[] { startTime, type });
		File baseDir = new File(m_baseDir, path);

		return baseDir.listFiles();
	}

	@Override
	public List<T> loadAll(ReportDelegate<T> delegate, ReportPeriod period, Date startTime, String domain)
	      throws IOException {
		switch (period) {
		case HOUR:
			return loadAllHourly(delegate, startTime, domain);
		case DAY:
			return loadAllDaily(delegate, startTime, domain);
		default:
			throw new UnsupportedOperationException("Not implemented yet!");
		}
	}

	private List<T> loadAllDaily(ReportDelegate<T> delegate, Date startTime, String domain) throws IOException {
		File file = getDailyReportFile(delegate, startTime, domain);
		String xml = Files.forIO().readFrom(file, "utf-8");

		if (xml != null) {
			List<T> reports = new ArrayList<T>(1);
			T report = delegate.parseXml(xml);

			reports.add(report);
			return reports;
		} else {
			return Collections.emptyList();
		}
	}

	private List<T> loadAllHourly(ReportDelegate<T> delegate, Date startTime, String domain) throws IOException {
		if (domain == null) {
			File[] files = getHourlyReportFiles(delegate, startTime);

			if (files != null && files.length > 0) {
				List<T> reports = new ArrayList<T>(1);

				for (File file : files) {
					String xml = Files.forIO().readFrom(file, "utf-8");

					if (xml != null) {
						T report = delegate.parseXml(xml);

						reports.add(report);
					}
				}

				return reports;
			}
		} else {
			File file = getHourlyReportFile(delegate, startTime, domain);
			String xml = Files.forIO().readFrom(file, "utf-8");

			if (xml != null) {
				List<T> reports = new ArrayList<T>(1);
				T report = delegate.parseXml(xml);

				reports.add(report);
				return reports;
			}
		}

		return Collections.emptyList();
	}

	@Override
	public void store(ReportDelegate<T> delegate, ReportPeriod period, T report, ReportStoragePolicy policy)
	      throws IOException {
		if (!policy.forFile()) {
			return;
		}

		switch (period) {
		case HOUR:
			storeHourlyReport(delegate, report);
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

	private void storeHourlyReport(ReportDelegate<T> delegate, T report) throws IOException {
		File file = getHourlyReportFile(delegate, report.getStartTime(), report.getDomain());
		String xml = delegate.buildXml(report);

		Files.forIO().writeTo(file, xml);
	}
}

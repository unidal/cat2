package org.unidal.cat.spi.report.task.internals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.cat.dal.report.HistoryReportDao;
import org.unidal.cat.dal.report.HistoryReportDo;
import org.unidal.cat.dal.report.HistoryReportEntity;
import org.unidal.cat.dal.report.HourlyReportDao;
import org.unidal.cat.dal.report.HourlyReportDo;
import org.unidal.cat.dal.report.HourlyReportEntity;
import org.unidal.cat.spi.ReportConfiguration;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.task.ReportTask;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Files;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportTaskTracker.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultReportTaskTracker implements ReportTaskTracker {
	@Inject
	private ReportConfiguration m_config;

	@Inject
	private HourlyReportDao m_hourlyReportDao;

	@Inject
	private HistoryReportDao m_historyReportDao;

	private List<String> m_domains;

	private File m_file;

	private FileOutputStream m_out;

	@Override
	public void close() throws IOException {
		m_out.close();
		m_file.delete();
	}

	@Override
	public void done(String domain) throws IOException {
		m_out.write(domain.getBytes("utf-8"));
		m_out.write("\r\n".getBytes("utf-8"));
		m_out.flush();
	}

	@Override
	public List<String> getDomains() {
		return m_domains;
	}

	private File getTrackerFile(ReportTask task) {
		MessageFormat format = new MessageFormat("task/{0}/{1}/{2,date,yyyy-MM-dd}.txt");
		Object[] args = { task.getReportName(), task.getTargetPeriod().getName(), task.getTargetStartTime() };
		String path = format.format(args);

		return new File(m_config.getBaseDataDir(), path);
	}

	private List<String> loadDomains(ReportTask task) throws DalException {
		ReportPeriod period = task.getSourcePeriod();
		Date start = task.getTargetStartTime();
		Date end = period.getNextStartTime(task.getTargetStartTime());
		List<String> domains = new ArrayList<String>(1024);

		if (period == ReportPeriod.HOUR) {
			List<HourlyReportDo> reports = m_hourlyReportDao.findAllByNameAndDateRange(task.getReportName(), start, end,
			      HourlyReportEntity.READSET_DOMAIN);

			for (HourlyReportDo report : reports) {
				domains.add(report.getDomain());
			}
		} else {
			List<HistoryReportDo> reports = m_historyReportDao.findAllByTypeAndNameAndDateRange(period.getId(),
			      task.getReportName(), start, end, HistoryReportEntity.READSET_DOMAIN);

			for (HistoryReportDo report : reports) {
				domains.add(report.getDomain());
			}
		}

		return domains;
	}

	@Override
	public void open(ReportTask task) throws IOException {
		try {
			m_domains = loadDomains(task);
		} catch (DalException e) {
			throw new IOException("Unable to fetch domains for " + task + "!", e);
		}

		m_file = getTrackerFile(task);

		if (m_file.exists()) {
			String content = Files.forIO().readFrom(m_file, "utf-8");
			List<String> done = Splitters.by("\r\n").trim().noEmptyItem().split(content);

			m_domains.removeAll(done);
		}

		m_file.getParentFile().mkdirs();
		m_out = new FileOutputStream(m_file, true);
	}
}

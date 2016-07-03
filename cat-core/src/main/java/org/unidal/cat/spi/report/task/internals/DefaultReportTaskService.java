package org.unidal.cat.spi.report.task.internals;

import java.util.Date;

import org.unidal.cat.dal.report.ReportTaskDao;
import org.unidal.cat.dal.report.ReportTaskDo;
import org.unidal.cat.dal.report.ReportTaskEntity;
import org.unidal.cat.spi.report.task.ReportTask;
import org.unidal.cat.spi.report.task.ReportTaskService;
import org.unidal.cat.spi.report.task.ReportTaskStatus;
import org.unidal.helper.Inets;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = ReportTaskService.class)
public class DefaultReportTaskService implements ReportTaskService {
	@Inject
	private ReportTaskDao m_dao;

	@Override
	public void add(String type, String report, Date scheduleDate) throws Exception {
	}

	@Override
	public void complete(ReportTask task) throws Exception {
		ReportTaskDo t = m_dao.createLocal();

		t.setKeyId(task.getId());
		t.setOldStatus(ReportTaskStatus.DOING.getId());
		t.setStatus(ReportTaskStatus.DONE.getId());

		m_dao.updateByPKAndOldStatus(t, ReportTaskEntity.UPDATESET_FULL);
	}

	@Override
	public void fail(ReportTask task, String reason) throws Exception {
		ReportTaskDo t = m_dao.createLocal();

		t.setKeyId(task.getId());
		t.setOldStatus(ReportTaskStatus.DOING.getId());
		t.setStatus(ReportTaskStatus.FAILED.getId());
		t.setFailureCount(task.getFailureCount() + 1);
		t.setFailureReason(reason);

		m_dao.updateByPKAndOldStatus(t, ReportTaskEntity.UPDATESET_FULL);
	}

	@Override
	public ReportTask pull(String id) throws Exception {
		int todo = ReportTaskStatus.TODO.getId();
		String ip = Inets.IP4.getLocalHostAddress();

		ReportTaskDo t = m_dao.findByStatusAndConsumerIp(todo, null, ReportTaskEntity.READSET_FULL);

		t.setOldStatus(todo);
		t.setStatus(ReportTaskStatus.DOING.getId());
		t.setConsumerIp(ip);

		int rows = m_dao.updateByPKAndOldStatus(t, ReportTaskEntity.UPDATESET_FULL);

		if (rows == 1) {
			return new DefaultReportTask(t);
		} else {
			return null;
		}
	}
}

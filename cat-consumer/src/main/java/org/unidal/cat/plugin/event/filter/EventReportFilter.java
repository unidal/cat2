package org.unidal.cat.plugin.event.filter;

import org.unidal.cat.plugin.event.EventConstants;
import org.unidal.cat.report.ReportFilter;
import org.unidal.cat.report.spi.remote.RemoteContext;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.model.IVisitor;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;

@Named(type = ReportFilter.class, value = EventConstants.ID + ":report")
public class EventReportFilter implements ReportFilter<EventReport> {
	@Override
	public void applyTo(RemoteContext ctx, EventReport report) {
		String type = ctx.getProperty("type", null);
		String name = ctx.getProperty("name", null);
		String ip = ctx.getProperty("ip", null);
		int min = ctx.getIntProperty("min", -1);
		int max = ctx.getIntProperty("max", -1);

		IVisitor visitor = new Filter(type, name, ip, min, max);
		report.accept(visitor);
	}

	@Override
	public String getId() {
		return "report";
	}

	@Override
	public String getReportName() {
		return EventConstants.ID;
	}

	static class Filter extends BaseVisitor {
		private String m_ip;

		private String m_name;

		private String m_type;

		private int m_min;

		private int m_max;

		public Filter(String type, String name, String ip, int min, int max) {
			m_type = type;
			m_name = name;
			m_ip = ip;
			m_min = min;
			m_max = max;
		}

		@Override
		public void visitMachine(Machine machine) {
			if (m_ip == null || m_ip.equals(Constants.ALL)) {
				super.visitMachine(machine);
			} else if (machine.getIp().equals(m_ip)) {
				super.visitMachine(machine);
			}
		}

		@Override
		public void visitName(EventName name) {
			if (m_type != null) {
				super.visitName(name);
			}
		}

		@Override
		public void visitRange(Range range) {
			if (m_type != null && m_name != null) {
				int minute = range.getValue();

				if (m_min == -1 && m_max == -1) {
					super.visitRange(range);
				} else if (minute <= m_max && minute >= m_min) {
					super.visitRange(range);
				}
			}
		}

		@Override
		public void visitEventReport(EventReport eventReport) {
			synchronized (eventReport) {
				super.visitEventReport(eventReport);
			}
		}

		@Override
		public void visitType(EventType type) {
			if (m_type == null) {
				super.visitType(type);
			} else if (type.getId().equals(m_type)) {
				super.visitType(type);
			}
		}
	}
}

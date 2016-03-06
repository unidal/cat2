package org.unidal.cat.plugin.transaction.filter;

import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;

class TransactionHolder {
	private TransactionReport m_report;

	private Machine m_machine;

	private TransactionType m_type;

	private TransactionName m_name;

	private Range m_range;

	private Duration m_duration;

	public TransactionReport getReport() {
		return m_report;
	}

	public void setReport(TransactionReport report) {
		m_report = report;
	}

	public Machine getMachine() {
		return m_machine;
	}

	public void setMachine(Machine machine) {
		m_machine = machine;
	}

	public TransactionType getType() {
		return m_type;
	}

	public void setType(TransactionType type) {
		m_type = type;
	}

	public TransactionName getName() {
		return m_name;
	}

	public void setName(TransactionName name) {
		m_name = name;
	}

	public Range getRange() {
		return m_range;
	}

	public void setRange(Range range) {
		m_range = range;
	}

	public Duration getDuration() {
		return m_duration;
	}

	public void setDuration(Duration duration) {
		m_duration = duration;
	}
}

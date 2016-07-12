package org.unidal.cat.plugin.transaction.reducer;

import java.util.List;

import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.cat.spi.report.ReportReducer;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.model.entity.AllDuration;
import com.dianping.cat.consumer.transaction.model.entity.DomainStat;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultMerger;

public abstract class AbstractTransactionReducer implements ReportReducer<TransactionReport> {
	protected abstract int getRangeValue(TransactionReport report, Range range);

	@Override
	public String getReportName() {
		return TransactionConstants.NAME;
	}

	@Override
	public TransactionReport reduce(List<TransactionReport> reports) {
		TransactionReport r = new TransactionReport();

		if (!reports.isEmpty()) {
			TransactionReport first = reports.get(0);
			Merger merger = new Merger(r);

			r.setDomain(first.getDomain());

			for (TransactionReport report : reports) {
				report.accept(merger.setMapping(new ValueMapping(report)));
			}

			ReportPeriod period = getPeriod();

			r.setPeriod(period);
			r.setStartTime(period.getStartTime(first.getStartTime()));
		}

		return r;
	}

	protected static class Merger extends DefaultMerger {
		private RangeMapping m_mapping;

		public Merger(TransactionReport report) {
			super(report);
		}

		@Override
		protected void mergeDomainStat(DomainStat to, DomainStat from) {
			to.setTotalCount(from.getTotalCount() + to.getTotalCount());
			to.setFailCount(from.getFailCount() + to.getFailCount());

			if (from.getMin() < to.getMin()) {
				to.setMin(from.getMin());
			}

			if (from.getMax() > to.getMax()) {
				to.setMax(from.getMax());
			}

			to.setSum(to.getSum() + from.getSum());
			to.setSum2(to.getSum2() + from.getSum2());
			to.setTps(to.getTps() + from.getTps());

			if (to.getTotalCount() > 0) {
				to.setAvg(to.getSum() / to.getTotalCount());
			}
		}

		@Override
		protected void mergeDuration(Duration old, Duration duration) {
			old.setCount(old.getCount() + duration.getCount());
			old.setValue(duration.getValue());
		}

		@Override
		protected void mergeMachine(Machine old, Machine machine) {
		}

		@Override
		protected void mergeName(TransactionName old, TransactionName other) {
			long totalCount = old.getTotalCount() + other.getTotalCount();
			if (totalCount > 0) {
				double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value()
				      * other.getTotalCount();
				double line99Values = old.getLine99Value() * old.getTotalCount() + other.getLine99Value()
				      * other.getTotalCount();

				old.setLine95Value(line95Values / totalCount);
				old.setLine99Value(line99Values / totalCount);
			}

			old.setTotalCount(totalCount);
			old.setFailCount(old.getFailCount() + other.getFailCount());
			old.setTps(old.getTps() + other.getTps());

			if (other.getMin() < old.getMin()) {
				old.setMin(other.getMin());
			}

			if (other.getMax() > old.getMax()) {
				old.setMax(other.getMax());
				old.setSlowestMessageUrl(other.getSlowestMessageUrl());
			}

			old.setSum(old.getSum() + other.getSum());
			old.setSum2(old.getSum2() + other.getSum2());

			if (old.getTotalCount() > 0) {
				old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
				old.setAvg(old.getSum() / old.getTotalCount());
				old.setStd(std(old.getTotalCount(), old.getAvg(), old.getSum2(), old.getMax()));
			}

			if (old.getSuccessMessageUrl() == null) {
				old.setSuccessMessageUrl(other.getSuccessMessageUrl());
			}

			if (old.getFailMessageUrl() == null) {
				old.setFailMessageUrl(other.getFailMessageUrl());
			}
		}

		@Override
		protected void mergeRange(Range old, Range range) {
			old.setCount(old.getCount() + range.getCount());
			old.setFails(old.getFails() + range.getFails());
			old.setSum(old.getSum() + range.getSum());

			if (old.getCount() > 0) {
				old.setAvg(old.getSum() / old.getCount());
			}
		}

		Machine mergesForAllMachine(TransactionReport report) {
			Machine all = new Machine(Constants.ALL);

			for (Machine m : report.getMachines().values()) {
				if (!m.getIp().equals(Constants.ALL)) {
					visitMachineChildren(all, m);
				}
			}

			return all;
		}

		@Override
		protected void mergeType(TransactionType old, TransactionType other) {
			long totalCount = old.getTotalCount() + other.getTotalCount();

			if (totalCount > 0) {
				double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value()
				      * other.getTotalCount();
				double line99Values = old.getLine99Value() * old.getTotalCount() + other.getLine99Value()
				      * other.getTotalCount();

				old.setLine95Value(line95Values / totalCount);
				old.setLine99Value(line99Values / totalCount);
			}

			old.setTotalCount(totalCount);
			old.setFailCount(old.getFailCount() + other.getFailCount());
			old.setTps(old.getTps() + other.getTps());

			if (other.getMin() < old.getMin()) {
				old.setMin(other.getMin());
			}

			if (other.getMax() > old.getMax()) {
				old.setMax(other.getMax());
				old.setSlowestMessageUrl(other.getSlowestMessageUrl());
			}

			old.setSum(old.getSum() + other.getSum());
			old.setSum2(old.getSum2() + other.getSum2());

			if (old.getTotalCount() > 0) {
				old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
				old.setAvg(old.getSum() / old.getTotalCount());
				old.setStd(std(old.getTotalCount(), old.getAvg(), old.getSum2(), old.getMax()));
			}

			if (old.getSuccessMessageUrl() == null) {
				old.setSuccessMessageUrl(other.getSuccessMessageUrl());
			}

			if (old.getFailMessageUrl() == null) {
				old.setFailMessageUrl(other.getFailMessageUrl());
			}
		}

		public Merger setMapping(RangeMapping mapping) {
			m_mapping = mapping;
			return this;
		}

		double std(long count, double avg, double sum2, double max) {
			double value = sum2 / count - avg * avg;

			if (value <= 0 || count <= 1) {
				return 0;
			} else if (count == 2) {
				return max - avg;
			} else {
				return Math.sqrt(value);
			}
		}

		protected void visitNameChildren(TransactionName to, TransactionName from) {
			for (Range source : from.getRanges()) {
				int value = m_mapping.getValue(source);
				Range r = to.findOrCreateRange(value);

				getObjects().push(r);
				source.accept(this);
				getObjects().pop();
			}

			for (Duration source : from.getDurations().values()) {
				Duration target = to.findDuration(source.getValue());

				if (target == null) {
					target = new Duration(source.getValue());
					to.addDuration(target);
				}

				getObjects().push(target);
				source.accept(this);
				getObjects().pop();
			}

			for (AllDuration source : from.getAllDurations().values()) {
				AllDuration target = to.findAllDuration(source.getValue());

				if (target == null) {
					target = new AllDuration(source.getValue());
					to.addAllDuration(target);
				}

				getObjects().push(target);
				source.accept(this);
				getObjects().pop();
			}
		}
	}

	protected static interface RangeMapping {
		public int getValue(Range range);
	}

	class ValueMapping implements RangeMapping {
		private TransactionReport m_report;

		public ValueMapping(TransactionReport report) {
			m_report = report;
		}

		@Override
		public int getValue(Range range) {
			return getRangeValue(m_report, range);
		}
	}
}

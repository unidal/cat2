package org.unidal.cat.plugin.transaction.filter;

import java.util.Map;

import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;

@Named(type = TransactionReportHelper.class)
public class TransactionReportHelper {
	public void mergeName(TransactionName old, TransactionName other) {
		long totalCountSum = old.getTotalCount() + other.getTotalCount();
		if (totalCountSum > 0) {
			double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value()
			      * other.getTotalCount();
			double line99Values = old.getLine99Value() * old.getTotalCount() + other.getLine99Value()
			      * other.getTotalCount();

			old.setLine95Value(line95Values / totalCountSum);
			old.setLine99Value(line99Values / totalCountSum);
		}

		old.setTotalCount(totalCountSum);
		old.setFailCount(old.getFailCount() + other.getFailCount());
		old.setTps(old.getTps() + other.getTps());

		if (other.getMin() < old.getMin()) {
			old.setMin(other.getMin());
		}

		if (other.getMax() > old.getMax()) {
			old.setMax(other.getMax());
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

	public void mergeType(TransactionType old, TransactionType other) {
		long totalCountSum = old.getTotalCount() + other.getTotalCount();
		if (totalCountSum > 0) {
			double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value()
			      * other.getTotalCount();
			double line99Values = old.getLine99Value() * old.getTotalCount() + other.getLine99Value()
			      * other.getTotalCount();

			old.setLine95Value(line95Values / totalCountSum);
			old.setLine99Value(line99Values / totalCountSum);
		}

		old.setTotalCount(totalCountSum);
		old.setFailCount(old.getFailCount() + other.getFailCount());
		old.setTps(old.getTps() + other.getTps());

		if (other.getMin() < old.getMin()) {
			old.setMin(other.getMin());
		}

		if (other.getMax() > old.getMax()) {
			old.setMax(other.getMax());
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

	public void mergeDurations(Map<Integer, Duration> old, Map<Integer, Duration> other) {
		for (Map.Entry<Integer, Duration> e : other.entrySet()) {
			Integer key = e.getKey();
			Duration duration = e.getValue();
			Duration oldDuration = old.get(key);

			if (oldDuration == null) {
				oldDuration = new Duration(duration.getValue());
				old.put(key, oldDuration);
			}

			oldDuration.setCount(oldDuration.getCount() + duration.getCount());
		}
	}

	public void mergeRanges(Map<Integer, Range> old, Map<Integer, Range> other) {
		for (Map.Entry<Integer, Range> e : other.entrySet()) {
			Integer key = e.getKey();
			Range duration = e.getValue();
			Range oldRange = old.get(key);

			if (oldRange == null) {
				oldRange = new Range(duration.getValue());
				old.put(key, oldRange);
			}

			oldRange.setCount(oldRange.getCount() + duration.getCount());
			oldRange.setFails(oldRange.getFails() + duration.getFails());
			oldRange.setSum(oldRange.getSum() + duration.getSum());

			if (oldRange.getCount() > 0) {
				oldRange.setAvg(oldRange.getSum() / oldRange.getCount());
			}
		}
	}
}

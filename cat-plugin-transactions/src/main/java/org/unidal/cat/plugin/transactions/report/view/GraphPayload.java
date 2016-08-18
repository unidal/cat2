package org.unidal.cat.plugin.transactions.report.view;

import java.util.HashMap;
import java.util.Map;

import org.unidal.cat.core.report.view.svg.AbstractGraphPayload;
import org.unidal.cat.plugin.transaction.model.entity.Duration;
import org.unidal.cat.plugin.transaction.model.entity.Range;
import org.unidal.cat.plugin.transaction.model.entity.TransactionName;

public class GraphPayload {
	abstract static class AbstractPayload extends AbstractGraphPayload {
		private final TransactionName m_name;

		public AbstractPayload(String title, String axisXLabel, String axisYLabel, TransactionName name) {
			super(title, axisXLabel, axisYLabel);

			m_name = name;
		}

		@Override
		public String getAxisXLabel(int index) {
			if (index % 5 == 0 && index < 61) {
				return String.valueOf(index);
			} else {
				return "";
			}
		}

		@Override
		public int getDisplayHeight() {
			return (int) (super.getDisplayHeight() * 0.7);
		}

		@Override
		public int getDisplayWidth() {
			return (int) (super.getDisplayWidth() * 0.7);
		}

		@Override
		public String getIdPrefix() {
			return m_name.getId() + "_" + super.getIdPrefix();
		}

		protected TransactionName getTransactionName() {
			return m_name;
		}

		@Override
		public int getWidth() {
			return super.getWidth() + 120;
		}

		@Override
		public boolean isStandalone() {
			return false;
		}
	}

	public static class AverageTimePayload extends AbstractPayload {
		public AverageTimePayload(String title, String axisXLabel, String axisYLabel, TransactionName name) {
			super(title, axisXLabel, axisYLabel, name);
		}

		@Override
		public int getOffsetY() {
			return getDisplayHeight() + 20;
		}

		@Override
		protected double[] loadValues() {
			double[] values = new double[60];

			for (Range range : getTransactionName().getRanges()) {
				int value = range.getValue();

				values[value] += range.getAvg();
			}

			return values;
		}
	}

	public static class DurationPayload extends AbstractPayload {

		private Map<Integer, Integer> m_map = new HashMap<Integer, Integer>();

		public DurationPayload(String title, String axisXLabel, String axisYLabel, TransactionName name) {
			super(title, axisXLabel, axisYLabel, name);
			int k = 1;

			m_map.put(0, 0);

			for (int i = 0; i < 17; i++) {
				m_map.put(k, i);
				k <<= 1;
			}
		}

		@Override
		public String getAxisXLabel(int index) {
			if (index == 0) {
				return "0";
			}

			int k = 1;

			for (int i = 1; i < index; i++) {
				k <<= 1;
			}

			return String.valueOf(k);
		}

		@Override
		public boolean isAxisXLabelRotated() {
			return true;
		}

		@Override
		public boolean isAxisXLabelSkipped() {
			return false;
		}

		@Override
		protected double[] loadValues() {
			double[] values = new double[17];

			for (Duration duration : getTransactionName().getDurations().values()) {
				int d = duration.getValue();
				Integer k = m_map.get(d);

				if (k != null) {
					values[k] += duration.getCount();
				}
			}

			return values;
		}
	}

	public final static class FailurePayload extends AbstractPayload {
		public FailurePayload(String title, String axisXLabel, String axisYLabel, TransactionName name) {
			super(title, axisXLabel, axisYLabel, name);
		}

		@Override
		public int getOffsetX() {
			return getDisplayWidth();
		}

		@Override
		public int getOffsetY() {
			return getDisplayHeight() + 20;
		}

		@Override
		protected double[] loadValues() {
			double[] values = new double[60];

			for (Range range : getTransactionName().getRanges()) {
				int value = range.getValue();

				values[value] += range.getFails();
			}

			return values;
		}
	}

	public final static class HitPayload extends AbstractPayload {
		public HitPayload(String title, String axisXLabel, String axisYLabel, TransactionName name) {
			super(title, axisXLabel, axisYLabel, name);
		}

		@Override
		public int getOffsetX() {
			return getDisplayWidth();
		}

		@Override
		protected double[] loadValues() {
			double[] values = new double[60];

			for (Range range : getTransactionName().getRanges()) {
				int value = range.getValue();

				values[value] += range.getCount();
			}

			return values;
		}
	}
}

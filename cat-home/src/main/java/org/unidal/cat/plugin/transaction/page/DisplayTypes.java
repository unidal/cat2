package org.unidal.cat.plugin.transaction.page;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.cat.plugin.transaction.model.entity.Machine;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.model.entity.TransactionType;

public class DisplayTypes {

	private Set<String> m_ips = new HashSet<String>();

	private List<TransactionTypeModel> m_results = new ArrayList<TransactionTypeModel>();

	public DisplayTypes display(String sorted, String ip, TransactionReport report) {
		Machine machine = report.getMachines().get(ip);

		if (machine == null) {
			return this;
		}

		m_ips = report.getIps();

		Map<String, TransactionType> types = machine.getTypes();

		if (types != null) {
			for (Entry<String, TransactionType> entry : types.entrySet()) {
				m_results.add(new TransactionTypeModel(entry.getKey(), entry.getValue()));
			}
		}

		if (sorted == null) {
			sorted = "avg";
		}

		Collections.sort(m_results, new TransactionTypeComparator(sorted));
		return this;
	}

	public Set<String> getIps() {
		return m_ips;
	}

	public List<TransactionTypeModel> getResults() {
		return m_results;
	}

	public static class TransactionTypeComparator implements Comparator<TransactionTypeModel> {

		private String m_sorted;

		public TransactionTypeComparator(String type) {
			m_sorted = type;
		}

		@Override
		public int compare(TransactionTypeModel m1, TransactionTypeModel m2) {
			if (m_sorted.equals("type")) {
				return m1.getType().compareTo(m2.getType());
			}
			if (m_sorted.equals("total")) {
				long value = m2.getDetail().getTotalCount() - m1.getDetail().getTotalCount();

				if (value > 0) {
					return 1;
				} else if (value < 0) {
					return -1;
				} else {
					return 0;
				}
			}
			if (m_sorted.equals("failure")) {
				return (int) (m2.getDetail().getFailCount() - m1.getDetail().getFailCount());
			}
			if (m_sorted.equals("failurePercent")) {
				return Double.compare(m2.getDetail().getFailPercent(), m1.getDetail().getFailPercent());
			}
			if (m_sorted.equals("avg")) {
				return Double.compare(m2.getDetail().getAvg(), -m1.getDetail().getAvg());
			}
			if (m_sorted.equals("95line")) {
				return Double.compare(m2.getDetail().getLine95Value(), m1.getDetail().getLine95Value());
			}
			if (m_sorted.equals("99line")) {
				return Double.compare(m2.getDetail().getLine99Value(), m1.getDetail().getLine99Value());
			}
			if (m_sorted.equals("min")) {
				return Double.compare(m2.getDetail().getMin(), m1.getDetail().getMin());
			}
			if (m_sorted.equals("max")) {
				return Double.compare(m2.getDetail().getMax(), m1.getDetail().getMax());
			}
			if (m_sorted.equals("std")) {
				return Double.compare(m2.getDetail().getStd(), m1.getDetail().getStd());
			}
			return 0;
		}
	}

	public static class TransactionTypeModel {
		private TransactionType m_detail;

		private String m_type;

		public TransactionTypeModel() {
		}

		public TransactionTypeModel(String str, TransactionType detail) {
			m_type = str;
			m_detail = detail;
		}

		public TransactionType getDetail() {
			return m_detail;
		}

		public String getType() {
			try {
				return URLEncoder.encode(m_type, "utf-8");
			} catch (Exception e) {
				return m_type;
			}
		}
	}
}

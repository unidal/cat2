package com.dianping.cat.analyzer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class TransactionAggregator {

	private static ConcurrentHashMap<String, TransactionData> s_transactions = new ConcurrentHashMap<String, TransactionData>();

	private static String buildKey(String type, String name) {
		return type + ',' + name;
	}

	private static TransactionData createTransactionData(String type, String name) {
		return new TransactionData(type, name);
	}

	private static Map<String, TransactionData> getAndResetTransactions() {
		Map<String, TransactionData> cloned = s_transactions;

		synchronized (TransactionAggregator.class) {
			s_transactions = new ConcurrentHashMap<String, TransactionData>();

			for (Map.Entry<String, TransactionData> entry : cloned.entrySet()) {
				String key = entry.getKey();
				TransactionData data = entry.getValue();

				if (s_transactions.size() < 2000 && data.getCount().get() > 0) {
					s_transactions.put(key, createTransactionData(data.getType(), data.getName()));
				}
			}
		}
		return cloned;
	}

	public static void logTransaction(Transaction t) {
		makeSureTransactionExist(t).add(t);
	}

	private static TransactionData makeSureTransactionExist(Transaction t) {
		String key = buildKey(t.getType(), t.getName());
		TransactionData item = s_transactions.get(key);

		if (null == item) {
			synchronized (TransactionAggregator.class) {
				item = s_transactions.get(key);

				if (null == item) {
					item = createTransactionData(t.getType(), t.getName());

					s_transactions.put(key, item);
				}
			}
		}
		return item;
	}

	public static void sendTransactionData() {
		Map<String, TransactionData> transactions = getAndResetTransactions();

		if (transactions.size() > 0) {
			Transaction t = Cat.newTransaction("_CatMergeTree", "_CatMergeTree");
			Cat.getManager().getThreadLocalMessageTree().setDiscard(false);

			for (TransactionData data : transactions.values()) {
				if (data.getCount().get() > 0) {
					Transaction tmp = Cat.newTransaction(data.getType(), data.getName());
					StringBuilder sb = new StringBuilder(32);

					sb.append(CatConstants.BATCH_FLAG).append(data.getCount().get()).append(CatConstants.SPLIT);
					sb.append(data.getFail().get()).append(CatConstants.SPLIT);
					sb.append(data.getSum().get());

					tmp.addData(sb.toString());
					tmp.setStatus(Message.SUCCESS);
					tmp.complete();
				}
			}
			t.setStatus(Message.SUCCESS);
			t.complete();
		}
	}

	public static class TransactionData {
		private String m_key;

		private String m_type;

		private String m_name;

		private AtomicInteger m_count = new AtomicInteger();

		private AtomicInteger m_fail = new AtomicInteger();

		private AtomicLong m_sum = new AtomicLong();

		public TransactionData(String type, String name) {
			m_type = type;
			m_name = name;
			m_key = buildKey(type, name);
		}

		public TransactionData add(Transaction t) {
			m_count.incrementAndGet();
			m_sum.getAndAdd(t.getDurationInMillis());

			if (!t.isSuccess()) {
				m_fail.incrementAndGet();
			}
			return this;
		}

		public AtomicInteger getCount() {
			return m_count;
		}

		public AtomicInteger getFail() {
			return m_fail;
		}

		public String getKey() {
			return m_key;
		}

		public String getName() {
			return m_name;
		}

		public AtomicLong getSum() {
			return m_sum;
		}

		public String getType() {
			return m_type;
		}
	}

}

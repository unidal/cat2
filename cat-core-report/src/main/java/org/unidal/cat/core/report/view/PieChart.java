package org.unidal.cat.core.report.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.dianping.cat.helper.JsonBuilder;

public class PieChart {
	private List<Item> m_items = new ArrayList<Item>();

	private transient int MAX_SIZE = 20;

	public void addItem(String id, long total) {
		m_items.add(new Item(id).setNumber(total));
	}

	public String getJson() {
		return new JsonBuilder().toJson(this);
	}

	public void prepare() {
		Collections.sort(m_items, new ItemCompartor());

		int size = m_items.size();

		if (size > MAX_SIZE) {
			for (int i = 0; i < MAX_SIZE; i++) {
				m_items.add(m_items.get(i));
			}

			Item item = new Item("Other");
			double sum = 0;

			for (int i = MAX_SIZE; i < size; i++) {
				Item temp = m_items.get(i);

				sum += temp.getNumber();
			}

			m_items.add(item.setNumber(sum));
		}
	}

	public static class Item {
		private String m_title;

		private double m_number;

		public Item(String title) {
			m_title = title;
		}

		public double getNumber() {
			return m_number;
		}

		public String getTitle() {
			return m_title;
		}

		public Item setNumber(double number) {
			m_number = number;
			return this;
		}
	}

	public static class ItemCompartor implements Comparator<Item> {
		@Override
		public int compare(Item o1, Item o2) {
			return (int) (o2.getNumber() - o1.getNumber());
		}
	}
}

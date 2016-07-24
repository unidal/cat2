package org.unidal.cat.plugin.transaction.view;

import java.util.List;

public interface TableViewModel<T> {
	public int getCount();

	public List<T> getRows();
}

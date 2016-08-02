package org.unidal.cat.core.report.view;

import java.util.List;

public interface TableViewModel<T> {
	public int getCount();

	public List<T> getRows();
}

package org.unidal.cat.core.report.nav;

import java.util.List;
import java.util.Set;

public interface GroupBar {
	public String getActiveGroup();

	public List<String> getActiveGroupItems();

	public List<String> getGroups();

	public String getId();

	public void initialize(String domain, String group, Set<String> items);
}

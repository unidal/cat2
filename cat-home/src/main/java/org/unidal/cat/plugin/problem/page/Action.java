package org.unidal.cat.plugin.problem.page;

public enum Action implements org.unidal.web.mvc.Action {
	DETAIL("detail", false),

	GROUP("group", false),

	THREAD("thread", false),

	HOUR_GRAPH("hourlyGraph", false),

	HISTORY_REPORT("history", true),

	HISTORY_GRAPH("historyGraph", true),

	HOURLY_REPORT("view", false);

	private String m_name;

	private boolean m_history;

	public static Action getByName(String name, Action defaultAction) {
		for (Action action : Action.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultAction;
	}

	Action(String name, boolean history) {
		m_name = name;
		m_history = history;
	}

	@Override
	public String getName() {
		return m_name;
	}

	public boolean isHistory() {
		return m_history;
	}
}

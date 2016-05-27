package org.unidal.cat.plugin.event.page;

public enum Action implements org.unidal.web.mvc.Action {
    HOURLY_REPORT("view", false),

    HOURLY_GRAPH("graphs", false),

    HISTORY_REPORT("history", true),

    HISTORY_GRAPH("historyGraph", true);

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

    private Action(String name, boolean history) {
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

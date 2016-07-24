package org.unidal.cat.plugin.transaction.view.svg;

public interface GraphBuilder {
	public String build(GraphPayload payload);

	public void setGraphType(int GraphType);
}

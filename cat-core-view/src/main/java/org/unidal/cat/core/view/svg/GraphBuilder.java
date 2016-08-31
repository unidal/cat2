package org.unidal.cat.core.view.svg;

public interface GraphBuilder {
	public String build(GraphPayload payload);

	public void setGraphType(int GraphType);
}

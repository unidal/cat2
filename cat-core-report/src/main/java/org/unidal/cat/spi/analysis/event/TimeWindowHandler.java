package org.unidal.cat.spi.analysis.event;

public interface TimeWindowHandler {
	public void onTimeWindowEnter(int hour);

	public void onTimeWindowExit(int hour);
}

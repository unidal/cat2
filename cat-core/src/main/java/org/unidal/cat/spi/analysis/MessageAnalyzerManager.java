package org.unidal.cat.spi.analysis;

import java.util.List;

public interface MessageAnalyzerManager {
	public void doCheckpoint(int hour, boolean atEnd);

	public List<MessageAnalyzer> getAnalyzers(int hour);
}

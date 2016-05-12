package org.unidal.cat.spi.analysis;

import org.unidal.cat.spi.analysis.pipeline.Pipeline;

import java.util.List;

public interface PipelineManager {
    public List<Pipeline> getPipelines(int hour);
}

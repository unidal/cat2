package org.unidal.cat.plugin.event;

import org.unidal.cat.spi.analysis.pipeline.AbstractPipeline;
import org.unidal.cat.spi.analysis.pipeline.Pipeline;
import org.unidal.lookup.annotation.Named;

@Named(type = Pipeline.class, value = EventConstants.NAME, instantiationStrategy = Named.PER_LOOKUP)
public class EventPipeline extends AbstractPipeline {
	@Override
	protected void beforeCheckpoint() {

	}

	@Override
	protected void afterCheckpoint() {

	}
}

package org.unidal.cat.plugin.event;

import org.unidal.cat.spi.analysis.pipeline.AbstractPipeline;
import org.unidal.cat.spi.analysis.pipeline.Pipeline;
import org.unidal.lookup.annotation.Named;

import java.io.IOException;

@Named(type = Pipeline.class, value = EventConstants.ID, instantiationStrategy = Named.PER_LOOKUP)
public class EventPipeline extends AbstractPipeline {
    @Override
    protected void beforeCheckpoint(){

    }

    @Override
    protected void afterCheckpoint() {

    }
}

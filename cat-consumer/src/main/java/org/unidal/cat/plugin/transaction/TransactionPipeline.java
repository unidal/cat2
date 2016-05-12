package org.unidal.cat.plugin.transaction;

import org.unidal.cat.spi.analysis.pipeline.AbstractPipeline;
import org.unidal.cat.spi.analysis.pipeline.Pipeline;
import org.unidal.lookup.annotation.Named;

@Named(type = Pipeline.class, value = TransactionConstants.NAME, instantiationStrategy = Named.PER_LOOKUP)
public class TransactionPipeline extends AbstractPipeline {
    @Override
    protected void beforeCheckpoint() {

    }
}

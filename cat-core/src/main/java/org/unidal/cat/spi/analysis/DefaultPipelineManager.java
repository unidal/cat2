package org.unidal.cat.spi.analysis;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.spi.analysis.event.TimeWindowHandler;
import org.unidal.cat.spi.analysis.event.TimeWindowManager;
import org.unidal.cat.spi.analysis.pipeline.Pipeline;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Named(type = PipelineManager.class)
public class DefaultPipelineManager extends ContainerHolder implements PipelineManager,
        TimeWindowHandler, Initializable, LogEnabled {
    @Inject
    private TimeWindowManager m_timeWindowManager;

    private ExecutorService m_executor;

    private Map<Integer, List<Pipeline>> m_map = new HashMap<Integer, List<Pipeline>>();

    private Logger m_logger;

    @Override
    public List<Pipeline> removePipelines(int hour) {
        List<Pipeline> pipelines = m_map.remove(hour);
        return pipelines;
    }

    @Override
    public List<Pipeline> getPipelines(int hour) {
        return m_map.get(hour);
    }

    @Override
    public void initialize() throws InitializationException {
        m_timeWindowManager.register(this);
    }

    @Override
    public void onTimeWindowEnter(int hour) {
        List<Pipeline> pipelines = new ArrayList<Pipeline>(super.lookupList(Pipeline.class));

        List<String> names = new ArrayList<String>();

        for (Pipeline pipeline : pipelines) {
            try {
                pipeline.initialize(hour);
                names.add(pipeline.getName());
            } catch (Throwable e) {
                String msg = String.format("Error when starting %s!", pipeline);

                m_logger.error(msg, e);
            }
        }

        m_map.put(hour, pipelines);
        m_logger.info("Following report pipelines configured: " + names);
    }

    @Override
    public void onTimeWindowExit(int hour) {
        List<Pipeline> pipelines = removePipelines(hour);

        for (Pipeline pipeline : pipelines) {
            try {
                pipeline.checkpoint(true);
            } catch (Exception e) {
                m_logger.error(String.format("Error when TimeWindowExit %s!", pipeline), e);
            }
        }

        for (Pipeline pipeline : pipelines) {
            super.release(pipeline);
            pipeline.destroy();
        }
    }

    @Override
    public void enableLogging(Logger logger) {
        m_logger = logger;
    }
}

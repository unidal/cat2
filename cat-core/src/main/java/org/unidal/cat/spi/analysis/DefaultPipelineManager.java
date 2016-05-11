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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named(type = PipelineManager.class)
public class DefaultPipelineManager extends ContainerHolder implements PipelineManager,
        TimeWindowHandler, Initializable, LogEnabled {

    private Logger m_logger;

    private List<String> m_names = new ArrayList<String>();

    private Map<Integer, List<Pipeline>> m_pipelines = new HashMap<Integer, List<Pipeline>>();

    @Inject
    private TimeWindowManager m_timeWindowManager;

    @Override
    public List<Pipeline> removePipelines(int hour) {
        List<Pipeline> pipelines = m_pipelines.remove(hour);
        return pipelines;
    }

    @Override
    public List<Pipeline> getPipelines(int hour) {
        List<Pipeline> pipelines = m_pipelines.get(hour);

        if (pipelines == null) {
            synchronized (this) {
                pipelines = m_pipelines.get(hour);

                if (pipelines == null) {
                    pipelines = new ArrayList<Pipeline>();

                    for (String name : m_names) {

                        Pipeline pipeline = lookup(Pipeline.class, name);
                            try {
                                pipeline.initialize(hour);
                                pipelines.add(pipeline);
                            } catch (Throwable e) {
                                String msg = String.format("Error when starting %s!", pipeline);
                                e.printStackTrace();
                                m_logger.error(msg, e);
                            }
                        }
                    }

                m_pipelines.put(hour, pipelines);
                }
            }
        return pipelines;
    }

    @Override
    public void initialize() throws InitializationException {
        m_timeWindowManager.register(this);
        Map<String, Pipeline> map = lookupMap(Pipeline.class);
        m_names.addAll(map.keySet());
        m_logger.info("Following report pipeline configured: " + m_names);
    }

    @Override
    public void onTimeWindowEnter(int hour) {

    }

    @Override
    public void onTimeWindowExit(int hour) {
        List<Pipeline> pipelines = removePipelines(hour);
        for(Pipeline pipeline : pipelines){
            try {
                super.release(pipeline);
                pipeline.checkpoint(true);
            } catch (IOException e) {
                e.printStackTrace();
                String msg = String.format("Error when TimeWindowExit %s!", pipeline);
                m_logger.error(msg, e);
            }
        }
    }

    @Override
    public void enableLogging(Logger logger) {
        m_logger = logger;
    }
}

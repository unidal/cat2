package org.unidal.cat.spi.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.cat.CatConstant;
import org.unidal.cat.spi.analysis.event.TimeWindowHandler;
import org.unidal.cat.spi.analysis.event.TimeWindowManager;
import org.unidal.cat.spi.analysis.pipeline.Pipeline;
import org.unidal.cat.spi.report.ReportConfiguration;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

@Named(type = PipelineManager.class)
public class DefaultPipelineManager extends ContainerHolder implements PipelineManager, TimeWindowHandler,
      Initializable, LogEnabled {
   @Inject
   private TimeWindowManager m_timeWindowManager;

   @Inject
   private ReportConfiguration m_config;

   private ExecutorService m_executor;

   private Map<Integer, List<Pipeline>> m_map = new HashMap<Integer, List<Pipeline>>();

   private Logger m_logger;

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   @Override
   public List<Pipeline> getPipelines(int hour) {
      return m_map.get(hour);
   }

   @Override
   public void initialize() {
      m_timeWindowManager.register(this);
      m_executor = Threads.forPool().getFixedThreadPool(CatConstant.CAT + "-Pipeline", m_config.getCheckpointThreads());
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
      List<Pipeline> pipelines = m_map.remove(hour);
      CountDownLatch latch = new CountDownLatch(pipelines.size());
      List<Future<?>> futures = new ArrayList<Future<?>>(pipelines.size());

      for (Pipeline pipeline : pipelines) {
         futures.add(m_executor.submit(new CheckpointTask(pipeline, latch)));
      }

      try {
         latch.await(60 - 2 * 3 - 1, TimeUnit.MINUTES); // 53 minutes at most
      } catch (InterruptedException e) {
         for (Future<?> future : futures) {
            if (!future.isDone()) {
               future.cancel(true);
            }
         }
      } finally {
         for (Pipeline pipeline : pipelines) {
            super.release(pipeline);
            pipeline.destroy();
         }
      }
   }

   class CheckpointTask implements Task {
      private Pipeline m_pipeline;

      private CountDownLatch m_latch;

      public CheckpointTask(Pipeline pipeline, CountDownLatch latch) {
         m_pipeline = pipeline;
         m_latch = latch;
      }

      @Override
      public String getName() {
         return getClass().getSimpleName() + "-" + m_pipeline.getName();
      }

      @Override
      public void run() {
         Transaction t = Cat.newTransaction("CheckpointTask", m_pipeline.getName());

         try {
            m_pipeline.checkpoint(true);
            t.setStatus(Message.SUCCESS);
         } catch (Throwable e) {
            m_logger.error(String.format("Error when doing checkpoint of %s!", m_pipeline), e);
            Cat.logError(e);
            t.setStatus(e);
         } finally {
            m_latch.countDown();
            t.complete();
         }
      }

      @Override
      public void shutdown() {
      }
   }
}

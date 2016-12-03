package org.unidal.cat.core.alert.metric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.CatConstant;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = MetricsDispatcher.class)
public class DefaultMetricsDispatcher extends ContainerHolder implements MetricsDispatcher, Initializable {
   private Map<String, List<MetricsListener<Metrics>>> m_map = new HashMap<String, List<MetricsListener<Metrics>>>();

   @Override
   public void dispatch(Metrics metrics) {
      String clazz = metrics.getTypeClass();
      List<MetricsListener<Metrics>> listeners = m_map.get(clazz);

      if (listeners != null) {
         for (MetricsListener<Metrics> listener : listeners) {
            try {
               listener.onMetrics(metrics);
            } catch (Throwable e) {
               Cat.logError(e);
            }
         }
      }
   }

   @Override
   @SuppressWarnings({ "rawtypes", "unchecked" })
   public void initialize() throws InitializationException {
      List<MetricsListener> listeners = lookupList(MetricsListener.class);

      for (MetricsListener<Metrics> listener : listeners) {
         String type = listener.getType().getName();
         List<MetricsListener<Metrics>> list = m_map.get(type);

         if (list == null) {
            list = new ArrayList<MetricsListener<Metrics>>();
            m_map.put(type, list);
         }

         list.add(listener);

         if (listener instanceof Task) {
            Threads.forGroup(CatConstant.CAT).start((Task) listener);
         }
      }
   }

   @Override
   public void checkpoint() {
      for (List<MetricsListener<Metrics>> listeners : m_map.values()) {
         for (MetricsListener<Metrics> listener : listeners) {
            try {
               listener.checkpoint();
            } catch (Throwable e) {
               Cat.logError(e);
            }
         }
      }
   }
}

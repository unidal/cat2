package org.unidal.cat.core.alert.engine;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.unidal.cat.core.alert.model.entity.AlertMetric;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.espertech.esper.client.ConfigurationOperations;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

@Named(type = AlertRegistry.class)
public class DefaultAlertRegistry implements AlertRegistry {
   private Map<String, Class<?>> m_eventClasses = new HashMap<String, Class<?>>();

   @Override
   public Object buildEvent(AlertMetric metric) {
      Class<?> eventClass = m_eventClasses.get(metric.getTypeName());

      return newInstance(eventClass, metric);
   }

   private Object newInstance(Class<?> clazz, AlertMetric metric) {
      if (clazz != null) {
         try {
            Constructor<?> c = clazz.getDeclaredConstructor(AlertMetric.class);

            c.setAccessible(true);
            return c.newInstance(metric);
         } catch (Throwable e) {
            Cat.logError(e);
         }
      }

      return metric;
   }

   @Override
   public void register(EPServiceProvider esper, AlertListener listener) {
      String eventName = listener.getEventName();
      Class<?> eventClass = listener.getEventClass();
      String statement = listener.getStatement();

      if (eventName == null || eventName.length() == 0) {
         throw new IllegalStateException("Event name can't be empty!");
      } else if (eventClass == null) {
         throw new IllegalStateException("Event class is required!");
      } else if (statement == null || statement.length() == 0) {
         throw new IllegalStateException("Event statement can't be empty!");
      }

      EPAdministrator admin = esper.getEPAdministrator();
      ConfigurationOperations config = admin.getConfiguration();

      config.addEventType(eventName, eventClass);
      m_eventClasses.put(eventName, eventClass);

      EPStatement state = admin.createEPL(statement);

      state.addListener(new AlertListenerWrapper(listener));
   }

   static class AlertListenerWrapper implements UpdateListener {
      private AlertListener m_listener;

      public AlertListenerWrapper(AlertListener listener) {
         m_listener = listener;
      }

      @Override
      public void update(EventBean[] newEvents, EventBean[] oldEvents) {
         m_listener.onEvent(new DefaultAlertContext(newEvents));
      }
   }

   static class DefaultAlertContext implements AlertContext {
      private EventBean[] m_events;

      public DefaultAlertContext(EventBean[] newEvents) {
         m_events = newEvents;
      }

      @Override
      @SuppressWarnings("unchecked")
      public <T> T getCell(int row, String property) {
         if (row < 0 || row >= m_events.length) {
            throw new IllegalStateException(String.format("Index(%s) is NOT in [0, %s]", row, m_events.length));
         }

         EventBean event = m_events[row];

         return (T) event.get(property);
      }

      @Override
      public int getRows() {
         return m_events.length;
      }

      @Override
      @SuppressWarnings("unchecked")
      public Map<String, Object> getRow(int row) {
         if (row < 0 || row >= m_events.length) {
            throw new IllegalStateException(String.format("Index(%s) is NOT in [0, %s]", row, m_events.length));
         }

         EventBean event = m_events[row];

         return (Map<String, Object>) event.getUnderlying();
      }
   }
}

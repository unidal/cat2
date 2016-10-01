package org.unidal.cat.core.alert.esper;

import org.junit.Before;

import com.espertech.esper.client.ConfigurationOperations;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.UpdateListener;

public abstract class EpserTestCase {
   private EPRuntime m_runtime;

   private EPServiceProvider m_service;

   @Before
   public void setup() {
      m_service = EPServiceProviderManager.getDefaultProvider();
      m_runtime = m_service.getEPRuntime();
   }

   protected void sendEvent(Object evt) {
      m_runtime.sendEvent(evt);
   }

   protected void registerEventType(String name, Class<?> type) {
      EPAdministrator admin = m_service.getEPAdministrator();
      ConfigurationOperations config = admin.getConfiguration();

      config.addEventType(name, type);
   }

   protected void register(String epl, UpdateListener listener) {
      EPAdministrator admin = m_service.getEPAdministrator();
      EPStatement state = admin.createEPL(epl);

      state.addListener(listener);
   }
}

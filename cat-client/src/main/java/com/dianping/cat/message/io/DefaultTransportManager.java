package com.dianping.cat.message.io;

import java.net.InetSocketAddress;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.config.ClientConfiguration;
import org.unidal.cat.config.ClientConfigurationManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = TransportManager.class)
public class DefaultTransportManager implements TransportManager, Initializable, LogEnabled {
   @Inject
   private ClientConfigurationManager m_manager;

   @Inject(type = MessageSender.class)
   private TcpSocketSender m_sender;

   private Logger m_logger;

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   @Override
   public MessageSender getSender() {
      return m_sender;
   }

   @Override
   public void initialize() throws InitializationException {
      ClientConfiguration config = m_manager.getConfig();

      if (config.isEnabled()) {
         List<InetSocketAddress> addresses = config.getServersForTree();

         if (addresses.isEmpty()) {
            throw new RuntimeException("No CAT servers configured!\r\n");
         } else {
            m_logger.info("CAT servers found: " + addresses);

            m_sender.setServerAddresses(addresses);
            m_sender.initialize();
         }
      }
   }

}

package org.unidal.cat.spi.transport;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.CatConstant;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.net.ServerTransport;
import org.unidal.net.Transports;

@Named
public class TcpSocketSkeleton implements Task, LogEnabled {
   @Inject
   private ServerTransportConfiguration m_config;

   @Inject
   private ServerTransportHub m_hub;

   private CountDownLatch m_active = new CountDownLatch(1);

   private CountDownLatch m_latch = new CountDownLatch(1);

   private Logger m_logger;

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   @Override
   public String getName() {
      return getClass().getSimpleName();
   }

   @Override
   public void run() {
      try {
         ServerTransport transport = Transports.asServer().name(CatConstant.CAT).bind(m_config.getTcpPort()) //
               .withBossThreads(m_config.getBossThreads()) //
               .withWorkerThreads(m_config.getWorkerThreads()) //
               .option(ChannelOption.SO_REUSEADDR, true) //
               .option(ChannelOption.TCP_NODELAY, true) //
               .option(ChannelOption.SO_KEEPALIVE, true) //
               .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) //
               .start(m_hub);

         m_active.await();
         transport.stop(3, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
         // ignore it
      } catch (Throwable e) {
         m_logger.error(String.format("Error occured when starting %s!", getClass()), e);
      } finally {
         m_latch.countDown();
      }
   }

   @Override
   public void shutdown() {
      m_active.countDown();

      try {
         m_latch.await(5, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
         // ignore it
      }
   }
}
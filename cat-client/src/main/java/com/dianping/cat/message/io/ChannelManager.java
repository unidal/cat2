package com.dianping.cat.message.io;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.Cat;
import org.unidal.cat.config.ClientConfigurationManager;
import org.unidal.helper.Threads.Task;
import org.unidal.tuple.Pair;

import com.dianping.cat.message.internal.MessageIdFactory;

public class ChannelManager implements Task {
   private boolean m_active = true;

   private ChannelHolder m_activeChannelHolder;

   private AtomicInteger m_attempts = new AtomicInteger();

   private Bootstrap m_bootstrap;

   private int m_channelStalledTimes;

   private ClientConfigurationManager m_configManager;

   private MessageIdFactory m_idFactory;

   // wait for server to startup in server mode
   private CountDownLatch m_catServerLatch;

   private Logger m_logger;

   public ChannelManager(Logger logger, List<InetSocketAddress> serverAddresses,
         ClientConfigurationManager configManager, MessageIdFactory idFactory, CountDownLatch catServerLatch) {
      m_logger = logger;
      m_configManager = configManager;
      m_idFactory = idFactory;
      m_catServerLatch = catServerLatch;

      EventLoopGroup group = new NioEventLoopGroup(1, new ThreadFactory() {
         @Override
         public Thread newThread(Runnable r) {
            Thread t = new Thread(r);

            t.setDaemon(true);
            return t;
         }
      });

      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(group).channel(NioSocketChannel.class);
      bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
      bootstrap.handler(new ChannelInitializer<Channel>() {
         @Override
         protected void initChannel(Channel ch) throws Exception {
         }
      });
      m_bootstrap = bootstrap;

      if (m_catServerLatch == null) {
         List<InetSocketAddress> addresses = m_configManager.getConfig().getServersForTree();

         if (addresses.size() > 0) {
            ChannelHolder holder = initChannel(addresses);

            if (holder != null) {
               m_activeChannelHolder = holder;
            } else {
               m_activeChannelHolder = new ChannelHolder().setServerAddresses(addresses);
            }
         } else {
            ChannelHolder holder = initChannel(serverAddresses);

            if (holder != null) {
               m_activeChannelHolder = holder;
            } else {
               m_activeChannelHolder = new ChannelHolder().setServerAddresses(serverAddresses);
            }
         }
      } else {
         m_activeChannelHolder = new ChannelHolder().setServerAddresses(serverAddresses);
      }
   }

   public ChannelFuture channel() {
      if (m_activeChannelHolder != null) {
         ChannelFuture future = m_activeChannelHolder.getActiveFuture();

         if (checkWritable(future)) {
            return future;
         }
      }
      return null;
   }

   private boolean checkActive(ChannelFuture future) {
      boolean isActive = false;

      if (future != null) {
         Channel channel = future.channel();

         if (channel.isActive() && channel.isOpen()) {
            isActive = true;
         } else {
            m_logger.warn("Channel buffer is not active ,current channel " + future.channel().remoteAddress());
         }
      }

      return isActive;
   }

   private void checkServerChanged() {
      Pair<Boolean, List<InetSocketAddress>> pair = serverAddressesChanged();

      if (pair.getKey()) {
         m_logger.info("router config changed: " + pair.getValue());

         List<InetSocketAddress> serverAddresses = pair.getValue();
         ChannelHolder newHolder = initChannel(serverAddresses);

         if (newHolder != null) {
            if (newHolder.isConnectChanged()) {
               ChannelHolder last = m_activeChannelHolder;

               m_activeChannelHolder = newHolder;
               closeChannelHolder(last);
               m_logger.info("switch active channel to " + m_activeChannelHolder);
            } else {
               m_activeChannelHolder = newHolder;
            }
         }
      }
   }

   private boolean checkWritable(ChannelFuture future) {
      boolean isWriteable = false;

      if (future != null) {
         Channel channel = future.channel();

         if (channel.isActive() && channel.isOpen()) {
            if (channel.isWritable()) {
               isWriteable = true;
            } else {
               channel.flush();
            }
         } else {
            int count = m_attempts.incrementAndGet();

            if (count % 1000 == 0 || count == 1) {
               m_logger.warn("Channel buffer is close when sending messages! Attempts: " + count);
            }
         }
      }

      return isWriteable;
   }

   private void closeChannel(ChannelFuture channel) {
      try {
         if (channel != null) {
            SocketAddress address = channel.channel().remoteAddress();

            if (address != null) {
               m_logger.info("Close channel " + address);
            }
            channel.channel().close();
         }
      } catch (Exception e) {
         // ignore
      }
   }

   private void closeChannelHolder(ChannelHolder channelHolder) {
      try {
         ChannelFuture channel = channelHolder.getActiveFuture();

         closeChannel(channel);
      } catch (Exception e) {
         // ignore
      }
   }

   private ChannelFuture createChannel(InetSocketAddress address) {
      m_logger.info("Start connecting to server " + address.toString());
      ChannelFuture future = null;

      try {
         future = m_bootstrap.connect(address);
         future.awaitUninterruptibly(100, TimeUnit.MILLISECONDS); // 100 ms

         if (!future.isSuccess()) {
            m_logger.error("Error when try connecting to " + address);
            closeChannel(future);
         } else {
            m_logger.info("Connected to CAT server at " + address);
            return future;
         }
      } catch (Throwable e) {
         m_logger.error("Error when connecting to server " + address.getAddress(), e);

         if (future != null) {
            closeChannel(future);
         }
      }
      return null;
   }

   private void doubleCheckActiveServer(ChannelHolder channelHolder) {
      try {
         if (isChannelStalled(channelHolder)) {
            closeChannelHolder(m_activeChannelHolder);
            channelHolder.setActiveIndex(-1);
         }
      } catch (Throwable e) {
         m_logger.error(e.getMessage(), e);
      }
   }

   @Override
   public String getName() {
      return getClass().getSimpleName();
   }

   private ChannelHolder initChannel(List<InetSocketAddress> addresses) {
      try {
         int len = addresses.size();

         for (int i = 0; i < len; i++) {
            InetSocketAddress address = addresses.get(i);
            String hostAddress = address.getAddress().getHostAddress();
            ChannelHolder holder = null;

            if (m_activeChannelHolder != null && hostAddress.equals(m_activeChannelHolder.getIp())) {
               holder = new ChannelHolder();
               holder.setActiveFuture(m_activeChannelHolder.getActiveFuture()).setConnectChanged(false);
            } else {
               ChannelFuture future = createChannel(address);

               if (future != null) {
                  holder = new ChannelHolder();
                  holder.setActiveFuture(future).setConnectChanged(true);
               }
            }
            if (holder != null) {
               holder.setActiveIndex(i).setIp(hostAddress);
               holder.setServerAddresses(addresses);

               m_logger.info("Success when init CAT server, new active holder " + holder.toString());
               return holder;
            }
         }
      } catch (Exception e) {
         m_logger.error(e.getMessage(), e);
      }

      try {
         StringBuilder sb = new StringBuilder();

         for (InetSocketAddress address : addresses) {
            sb.append(address.toString()).append(";");
         }
         m_logger.info("Error when init CAT server " + sb.toString());
      } catch (Exception e) {
         // ignore
      }
      return null;
   }

   private boolean isChannelStalled(ChannelHolder holder) {
      ChannelFuture future = holder.getActiveFuture();
      boolean active = checkActive(future);

      if (!active) {
         if ((++m_channelStalledTimes) % 3 == 0) {
            return true;
         } else {
            return false;
         }
      } else {
         if (m_channelStalledTimes > 0) {
            m_channelStalledTimes--;
         }
         return false;
      }
   }

   private void reconnectDefaultServer(ChannelFuture activeFuture, List<InetSocketAddress> serverAddresses) {
      try {
         int reconnectServers = m_activeChannelHolder.getActiveIndex();

         if (reconnectServers == -1) {
            reconnectServers = serverAddresses.size();
         }
         for (int i = 0; i < reconnectServers; i++) {
            ChannelFuture future = createChannel(serverAddresses.get(i));

            if (future != null) {
               ChannelFuture lastFuture = activeFuture;

               m_activeChannelHolder.setActiveFuture(future);
               m_activeChannelHolder.setActiveIndex(i);
               closeChannel(lastFuture);
               break;
            }
         }
      } catch (Throwable e) {
         m_logger.error(e.getMessage(), e);
      }
   }

   @Override
   public void run() {
      try {
         m_catServerLatch.await();
      } catch (InterruptedException e) {
         return;
      }

      while (m_active && Cat.isEnabled()) {
         // make save message id index asyc
         m_idFactory.saveMark();
         checkServerChanged();

         ChannelFuture activeFuture = m_activeChannelHolder.getActiveFuture();
         List<InetSocketAddress> serverAddresses = m_activeChannelHolder.getServerAddresses();

         doubleCheckActiveServer(m_activeChannelHolder);
         reconnectDefaultServer(activeFuture, serverAddresses);

         try {
            Thread.sleep(10 * 1000L); // check every 10 seconds
         } catch (InterruptedException e) {
            // ignore
         }
      }
   }

   private Pair<Boolean, List<InetSocketAddress>> serverAddressesChanged() {
      List<InetSocketAddress> addresses = m_configManager.getConfig().getServersForTree();

      if (!addresses.isEmpty() && !addresses.equals(m_activeChannelHolder.getServerAddresses())) {
         return new Pair<Boolean, List<InetSocketAddress>>(true, addresses);
      } else {
         return new Pair<Boolean, List<InetSocketAddress>>(false, addresses);
      }
   }

   @Override
   public void shutdown() {
      m_active = false;
   }

   public static class ChannelHolder {
      private ChannelFuture m_activeFuture;

      private int m_activeIndex = -1;

      private String m_activeServerConfig;

      private boolean m_connectChanged;

      private String m_ip;

      private List<InetSocketAddress> m_serverAddresses;

      public ChannelFuture getActiveFuture() {
         return m_activeFuture;
      }

      public int getActiveIndex() {
         return m_activeIndex;
      }

      public String getActiveServerConfig() {
         return m_activeServerConfig;
      }

      public String getIp() {
         return m_ip;
      }

      public List<InetSocketAddress> getServerAddresses() {
         return m_serverAddresses;
      }

      public boolean isConnectChanged() {
         return m_connectChanged;
      }

      public ChannelHolder setActiveFuture(ChannelFuture activeFuture) {
         m_activeFuture = activeFuture;
         return this;
      }

      public ChannelHolder setActiveIndex(int activeIndex) {
         m_activeIndex = activeIndex;
         return this;
      }

      public ChannelHolder setActiveServerConfig(String activeServerConfig) {
         m_activeServerConfig = activeServerConfig;
         return this;
      }

      public ChannelHolder setConnectChanged(boolean connectChanged) {
         m_connectChanged = connectChanged;
         return this;
      }

      public ChannelHolder setIp(String ip) {
         m_ip = ip;
         return this;
      }

      public ChannelHolder setServerAddresses(List<InetSocketAddress> serverAddresses) {
         m_serverAddresses = serverAddresses;
         return this;
      }

      public String toString() {
         StringBuilder sb = new StringBuilder();

         sb.append("active future :").append(m_activeFuture.channel().remoteAddress());
         sb.append(" index:").append(m_activeIndex);
         sb.append(" ip:").append(m_ip);
         sb.append(" server config:").append(m_activeServerConfig);
         return sb.toString();
      }
   }

   public class ClientMessageHandler extends SimpleChannelInboundHandler<Object> {

      @Override
      protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
         m_logger.info("receiver msg from server:" + msg);
      }
   }

}
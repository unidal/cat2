package com.dianping.cat.analysis;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.spi.transport.ServerTransportConfiguration;
import org.unidal.cat.transport.decode.DecodeHandler;
import org.unidal.cat.transport.decode.DecodeHandlerManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = TcpSocketReceiver.class)
public final class TcpSocketReceiver implements Initializable, LogEnabled {
   @Inject
   private DecodeHandlerManager m_manager;

   @Inject
   private ServerTransportConfiguration m_config;

   private ChannelFuture m_future;

   private EventLoopGroup m_bossGroup;

   private EventLoopGroup m_workerGroup;

   private int m_port;

   private Class<? extends ServerSocketChannel> m_channelClass;

   private Logger m_logger;

   public synchronized void destory() {
      try {
         m_future.channel().closeFuture();
         m_bossGroup.shutdownGracefully();
         m_workerGroup.shutdownGracefully();
         m_logger.info(String.format("Netty server stopped on port %s", m_port));
      } catch (Exception e) {
         m_logger.warn(e.getMessage(), e);
      }
   }

   @Override
   public void enableLogging(Logger logger) {
      m_logger = logger;
   }

   private boolean getOSMatches(String osNamePrefix) {
      String os = System.getProperty("os.name");

      if (os == null) {
         return false;
      }

      return os.startsWith(osNamePrefix);
   }

   @Override
   public void initialize() throws InitializationException {
      int bossThreads = m_config.getBossThreads();
      int workerThreads = m_config.getWorkerThreads();
      boolean linux = getOSMatches("Linux") || getOSMatches("LINUX");

      m_port = m_config.getTcpPort();
      m_bossGroup = linux ? new EpollEventLoopGroup(bossThreads) : new NioEventLoopGroup(bossThreads);
      m_workerGroup = linux ? new EpollEventLoopGroup(workerThreads) : new NioEventLoopGroup(workerThreads);
      m_channelClass = linux ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
   }

   public synchronized void setup() throws Exception {
      ServerBootstrap bootstrap = new ServerBootstrap();

      bootstrap.group(m_bossGroup, m_workerGroup).channel(m_channelClass);
      bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
         @Override
         protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast("decode", new MessageDecoder());
         }
      });

      bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
      bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
      bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
      bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

      try {
         m_future = bootstrap.bind(m_port).sync();
         m_logger.info(String.format("CAT is listening on port %s", m_port));
      } catch (Exception e) {
         m_logger.error(String.format("Error when binding to port %s!", m_port), e);

         throw e;
      }
   }

   public class MessageDecoder extends ByteToMessageDecoder {
      @Override
      protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
         if (buffer.readableBytes() < 4) {
            return;
         }

         buffer.markReaderIndex();

         int length = buffer.readInt();

         buffer.resetReaderIndex();

         if (buffer.readableBytes() < length + 4) {
            return;
         }

         buffer.readInt(); // get rid of length

         ByteBuf buf = buffer.readSlice(length);
         DecodeHandler handler = m_manager.getHandler(buf);

         if (handler != null) {
            buf.retain(); // hold reference to avoid being GC, the buf will be released in dump analyzer
            handler.handle(buf);
         }
      }
   }
}
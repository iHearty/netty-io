package cn.togeek.netty.client;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import cn.togeek.netty.Settings;
import cn.togeek.netty.TransportBase;
import cn.togeek.netty.exception.SettingsException;
import cn.togeek.netty.rpc.Transport;
import cn.togeek.netty.util.Strings;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TransportClient extends TransportBase<Bootstrap> {
   private static final Logger logger = Logger
      .getLogger(TransportClient.class.getName());

   private ScheduledExecutorService executor = Executors
      .newScheduledThreadPool(1);

   private EventLoopGroup workGroup;

   @Override
   public void start(final Settings settings) throws SettingsException {
      init(settings);

      try {
         bootstrap
            .connect(new InetSocketAddress(host(settings), port(settings)))
            .sync().channel()
            .closeFuture().sync();
      }
      catch(Exception e) {
         throw new RuntimeException("Failed to connect to [" + host(settings)
            + ", " + port(settings) + "]", e);
      }
      finally {
         workGroup.shutdownGracefully();

         executor.schedule(new Runnable() {
            @Override
            public void run() {
               try {
                  start(settings);
               }
               catch(Exception e) {
                  logger.log(Level.SEVERE, "Try restart failed.", e);
               }
            }
         }, 30, TimeUnit.SECONDS);
      }
   }

   @Override
   protected void init(Settings settings) throws SettingsException {
      this.workGroup = new NioEventLoopGroup();

      this.bootstrap = new Bootstrap()
         .group(workGroup)
         .channel(NioSocketChannel.class)
         .option(ChannelOption.SO_REUSEADDR, true)
         .option(ChannelOption.TCP_NODELAY, true);

      if(!Strings.isEmpty(settings.get(SO_SNDBUF))) {
         bootstrap.option(ChannelOption.SO_SNDBUF,
            settings.getAsInt(SO_SNDBUF, 8192));
      }

      if(!Strings.isEmpty(settings.get(SO_RCVBUF))) {
         bootstrap.option(ChannelOption.SO_RCVBUF,
            settings.getAsInt(SO_RCVBUF, 8192));
      }

      bootstrap.handler(new ClientChannelInitializer(
         Transport.Message.getDefaultInstance()));
   }
}
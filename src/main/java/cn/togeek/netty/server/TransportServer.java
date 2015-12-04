package cn.togeek.netty.server;

import java.net.InetSocketAddress;

import cn.togeek.netty.Settings;
import cn.togeek.netty.SettingsException;
import cn.togeek.netty.TransportBase;
import cn.togeek.netty.rpc.Transport;
import cn.togeek.netty.util.Strings;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class TransportServer extends TransportBase<ServerBootstrap> {
   private EventLoopGroup boosGroup;

   private EventLoopGroup workGroup;

   @Override
   public void start(Settings settings) throws SettingsException {
      init(settings);

      try {
         bootstrap.bind(new InetSocketAddress(host(settings), port(settings)))
            .sync()
            .channel()
            .closeFuture().sync();
      }
      catch(Exception e) {
         throw new RuntimeException(
            "Failed to bind to [" + host(settings) + ", " + port(settings)
               + "]",
            e);
      }
      finally {
         boosGroup.shutdownGracefully();
         workGroup.shutdownGracefully();
      }
   }

   @Override
   protected void init(Settings settings) throws SettingsException {
      this.boosGroup = new NioEventLoopGroup(1);
      this.workGroup = new NioEventLoopGroup();

      this.bootstrap = new ServerBootstrap()
         .group(boosGroup, workGroup)
         .channel(NioServerSocketChannel.class)
         .option(ChannelOption.SO_REUSEADDR, true)
         .option(ChannelOption.TCP_NODELAY, true)
         .option(ChannelOption.SO_KEEPALIVE, true)
         .childOption(ChannelOption.TCP_NODELAY, true)
         .childOption(ChannelOption.SO_KEEPALIVE, true);

      if(!Strings.isEmpty(settings.get(SO_BACKLOG))) {
         bootstrap.option(ChannelOption.SO_BACKLOG,
            settings.getAsInt(SO_BACKLOG, 100));
      }

      if(!Strings.isEmpty(settings.get(SO_SNDBUF))) {
         bootstrap.childOption(ChannelOption.SO_SNDBUF,
            settings.getAsInt(SO_SNDBUF, 8192));
      }

      if(!Strings.isEmpty(settings.get(SO_RCVBUF))) {
         bootstrap.childOption(ChannelOption.SO_RCVBUF,
            settings.getAsInt(SO_RCVBUF, 8192));
      }

      bootstrap.handler(new ChannelInitializer<ServerSocketChannel>() {
         @Override
         protected void initChannel(ServerSocketChannel channel)
            throws Exception
         {
            ChannelPipeline pipeline = channel.pipeline();
            pipeline.addLast(new LoggingHandler(LogLevel.INFO));
         }
      });

      bootstrap.childHandler(new ServerChannelInitializer(
         Transport.Message.getDefaultInstance()));
   }
}
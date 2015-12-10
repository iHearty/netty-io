package cn.togeek.netty.server;

import cn.togeek.netty.Settings;
import cn.togeek.netty.handler.NodeService;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class ChannelRegistryHandler extends ChannelHandlerAdapter {
   private Settings settings;

   public ChannelRegistryHandler(Settings settings) {
      this.settings = settings;
   }

   @Override
   public void channelActive(ChannelHandlerContext context) throws Exception {
      NodeService.INSTANCE.register(settings, context.channel());
      context.fireChannelActive();
   }
}
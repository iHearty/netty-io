package cn.togeek.netty.handler;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class ChannelRegistryHandler extends ChannelHandlerAdapter {
   @Override
   public void channelActive(ChannelHandlerContext context) throws Exception {
      TransportService.INSTANCE.addChannel(context.channel());
      context.fireChannelActive();
      GlobalObservable.INSTANCE.notifyObservers(context.channel().id());
   }
}
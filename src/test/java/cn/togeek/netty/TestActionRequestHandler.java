package cn.togeek.netty;

import cn.togeek.netty.handler.TransportRequestHandler;

import io.netty.channel.Channel;

public class TestActionRequestHandler
   implements TransportRequestHandler<TestActionRequest>
{
   @Override
   public void handle(TestActionRequest request, Channel channel)
      throws Exception
   {
      System.out.println(" request handle ");
   }
}
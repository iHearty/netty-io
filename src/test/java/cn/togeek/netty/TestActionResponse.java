package cn.togeek.netty;

import java.io.IOException;

import cn.togeek.netty.handler.TransportResponse;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TestActionResponse implements TransportResponse {
   @Override
   public void readFrom(ByteBuf in) throws IOException {
      System.out.println("## response read " + new String(in.array()));
   }

   @Override
   public ByteBuf writeTo() throws IOException {
      System.out.println("## response write ");
      return Unpooled.copiedBuffer("hi".getBytes());
   }
}
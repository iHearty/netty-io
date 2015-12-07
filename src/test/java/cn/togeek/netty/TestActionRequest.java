package cn.togeek.netty;

import java.io.IOException;

import org.omg.PortableInterceptor.TRANSPORT_RETRY;

import cn.togeek.netty.handler.TransportRequest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TestActionRequest implements TransportRequest, TRANSPORT_RETRY {
   @Override
   public void readFrom(ByteBuf in) throws IOException {
      System.out.println("$$ request read " + new String(in.array()));
   }

   @Override
   public ByteBuf writeTo() throws IOException {
      System.out.println("$$ request write ");
      return Unpooled.copiedBuffer("hello".getBytes());
   }
}
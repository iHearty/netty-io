package cn.togeek.netty;

import java.io.IOException;

import com.google.protobuf.ByteString;

import cn.togeek.netty.handler.TransportResponse;

public class TestActionResponse implements TransportResponse {
   @Override
   public void readFrom(ByteString in) throws IOException {
      System.out.println("## response read " + new String(in.toByteArray()));
   }

   @Override
   public ByteString writeTo() throws IOException {
      System.out.println("## response write ");
      return ByteString.copyFrom("hi".getBytes());
   }
}
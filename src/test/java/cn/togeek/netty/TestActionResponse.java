package cn.togeek.netty;

import java.io.IOException;

import com.google.protobuf.ByteString;

import cn.togeek.netty.handler.TransportResponse;

public class TestActionResponse implements TransportResponse {
   @Override
   public void readFrom(ByteString in) throws IOException {
      System.out.println("## response read");
   }

   @Override
   public void writeTo(ByteString out) throws IOException {
      System.out.println("## response write");
   }
}
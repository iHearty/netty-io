package cn.togeek.netty;

import java.io.IOException;

import org.omg.PortableInterceptor.TRANSPORT_RETRY;

import com.google.protobuf.ByteString;

import cn.togeek.netty.handler.TransportRequest;

public class TestActionRequest implements TransportRequest, TRANSPORT_RETRY {
   @Override
   public void readFrom(ByteString in) throws IOException {
      System.out.println("$$ request read " + new String(in.toByteArray()));
   }
}
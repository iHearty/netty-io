package cn.togeek.netty;

import java.io.IOException;

import com.google.protobuf.ByteString;

public interface Streamable {
   void readFrom(ByteString in) throws IOException;

   ByteString writeTo() throws IOException;
}
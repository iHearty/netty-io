package cn.togeek.netty;

import java.io.IOException;

import com.google.protobuf.ByteString;

public interface Readable {
   void readFrom(ByteString in) throws IOException;
}
package cn.togeek.netty;

import java.io.IOException;

import com.google.protobuf.ByteString;

public interface Writeable {
   ByteString writeTo() throws IOException;
}
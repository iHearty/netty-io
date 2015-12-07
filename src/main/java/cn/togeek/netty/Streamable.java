package cn.togeek.netty;

import java.io.IOException;

import io.netty.buffer.ByteBuf;

public interface Streamable {
   void readFrom(ByteBuf in) throws IOException;

   ByteBuf writeTo() throws IOException;
}
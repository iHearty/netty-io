package cn.togeek.netty.util;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;

public class ByteBufs {
   public static void writeString(String str, ByteBuf out) {
      if(str == null) {
         out.writeBoolean(false);
         return;
      }

      out.writeBoolean(true);
      byte[] bytes = str.getBytes(Charset.forName("UTF-8"));
      out.writeInt(bytes.length);
      out.writeBytes(bytes);
   }

   public static String readString(ByteBuf in) {
      if(!in.readBoolean()) {
         return null;
      }

      int length = in.readInt();
      byte[] bytes = new byte[length];
      in.readBytes(bytes);
      return new String(bytes);
   }
}
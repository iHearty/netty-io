package cn.togeek.netty.util;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;

public class ByteBufs {
   public static void writeString(String str, ByteBuf out) {
      if(str == null) {
         out.writeBoolean(false);
      }
      else {
         out.writeBoolean(true);

         try {
            byte[] bytes = str.getBytes("UTF-8");
            out.writeInt(bytes.length);
            out.writeBytes(bytes);
         }
         catch(UnsupportedEncodingException e) {
         }
      }
   }

   public static String readString(ByteBuf in) {
      if(in.readBoolean()) {
         final int size = in.readInt();
         byte[] bytes = new byte[size];
         in.readBytes(bytes);
         return new String(bytes);
      }

      return null;
   }
}
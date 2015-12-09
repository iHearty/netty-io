package cn.togeek.netty.util;

import org.apache.lucene.util.CharsRefBuilder;

import io.netty.buffer.ByteBuf;

public class ByteBufs {
   public static void writeString(String str, ByteBuf out) {
      int charCount = str.length();
      writeVInt(charCount, out);

      int c;

      for(int i = 0; i < charCount; i++) {
         c = str.charAt(i);

         if(c <= 0x007F) {
            out.writeByte((byte) c);
         }
         else if(c > 0x07FF) {
            out.writeByte((byte) (0xE0 | c >> 12 & 0x0F));
            out.writeByte((byte) (0x80 | c >> 6 & 0x3F));
            out.writeByte((byte) (0x80 | c >> 0 & 0x3F));
         }
         else {
            out.writeByte((byte) (0xC0 | c >> 6 & 0x1F));
            out.writeByte((byte) (0x80 | c >> 0 & 0x3F));
         }
      }
   }

   /**
    * Writes an int in a variable-length format. Writes between one and
    * five bytes. Smaller values take fewer bytes. Negative numbers
    * will always use all 5 bytes and are therefore better serialized
    * using {@link #writeInt}
    */
   public static void writeVInt(int i, ByteBuf buf) {
      while((i & ~0x7F) != 0) {
         buf.writeByte((byte) ((i & 0x7f) | 0x80));
         i >>>= 7;
      }

      buf.writeByte((byte) i);
   }

   private static final CharsRefBuilder spare = new CharsRefBuilder();

   public static String readString(ByteBuf in) {
      final int charCount = readVInt(in);
      spare.clear();
      spare.grow(charCount);
      int c;

      while(spare.length() < charCount) {
         c = in.readByte() & 0xff;

         switch(c >> 4) {
         case 0 :
         case 1 :
         case 2 :
         case 3 :
         case 4 :
         case 5 :
         case 6 :
         case 7 :
            spare.append((char) c);
            break;
         case 12 :
         case 13 :
            spare.append((char) ((c & 0x1F) << 6 | in.readByte() & 0x3F));
            break;
         case 14 :
            spare.append((char) ((c & 0x0F) << 12 | (in.readByte() & 0x3F) << 6
               | (in.readByte() & 0x3F) << 0));
            break;
         }
      }

      return spare.toString();
   }

   /**
    * Reads an int stored in variable-length format. Reads between one and
    * five bytes. Smaller values take fewer bytes. Negative numbers
    * will always use all 5 bytes and are therefore better serialized
    * using {@link #readInt}
    */
   public static int readVInt(ByteBuf buf) {
      byte b = buf.readByte();
      int i = b & 0x7F;

      if((b & 0x80) == 0) {
         return i;
      }

      b = buf.readByte();
      i |= (b & 0x7F) << 7;

      if((b & 0x80) == 0) {
         return i;
      }

      b = buf.readByte();
      i |= (b & 0x7F) << 14;

      if((b & 0x80) == 0) {
         return i;
      }

      b = buf.readByte();
      i |= (b & 0x7F) << 21;

      if((b & 0x80) == 0) {
         return i;
      }

      b = buf.readByte();
      assert (b & 0x80) == 0;
      return i | ((b & 0x7F) << 28);
   }
}
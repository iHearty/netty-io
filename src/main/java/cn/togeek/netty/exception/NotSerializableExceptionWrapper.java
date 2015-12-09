package cn.togeek.netty.exception;

import cn.togeek.netty.util.ByteBufs;

import io.netty.buffer.ByteBuf;

public class NotSerializableExceptionWrapper extends BaseException {
   private static final long serialVersionUID = 1L;

   private String name;

   public NotSerializableExceptionWrapper(Throwable other) {
      super(other.getMessage(), other.getCause());

      this.name = other.getClass().getName();
      setStackTrace(other.getStackTrace());

      for(Throwable otherSuppressed : other.getSuppressed()) {
         addSuppressed(otherSuppressed);
      }
   }

   public NotSerializableExceptionWrapper(String msg, Throwable cause) {
      super(msg, cause);
   }

   public String getExceptionName() {
      return name;
   }

   @Override
   public void writeTo(ByteBuf out) {
      super.writeTo(out);
      ByteBufs.writeString(name, out);
   }

   @Override
   public void readFrom(ByteBuf in) {
      this.name = ByteBufs.readString(in);
   }
}
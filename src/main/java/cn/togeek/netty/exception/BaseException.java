package cn.togeek.netty.exception;

import cn.togeek.netty.util.ByteBufs;

import io.netty.buffer.ByteBuf;

public class BaseException extends RuntimeException {
   private static final long serialVersionUID = 1L;

   public BaseException(Throwable cause) {
      super(cause);
   }

   public BaseException(String msg, Throwable cause) {
      super(msg, cause);
   }

   public void writeTo(ByteBuf out) {
      ByteBufs.writeString(this.getClass().getName(), out);
      ByteBufs.writeString(this.getMessage(), out);
      Exceptions.writeThrowable(this.getCause(), out);
      Exceptions.writeStackTraces(this, out);
   }

   public void readFrom(ByteBuf in) {
   }
}
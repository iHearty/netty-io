package cn.togeek.netty.exception;

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
      Exceptions.writeString(this.getClass().getName(), out);
      Exceptions.writeString(this.getMessage(), out);
      Exceptions.writeThrowable(this.getCause(), out);
      Exceptions.writeStackTraces(this, out);
   }

   public void readFrom(ByteBuf in) {
   }
}
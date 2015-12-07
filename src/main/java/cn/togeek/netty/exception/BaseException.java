package cn.togeek.netty.exception;

public class BaseException extends RuntimeException {
   private static final long serialVersionUID = 1L;

   public BaseException(Throwable cause) {
      super(cause);
   }

   public BaseException(String msg, Throwable cause) {
      super(msg, cause);
   }
}
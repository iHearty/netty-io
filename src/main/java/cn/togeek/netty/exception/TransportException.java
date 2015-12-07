package cn.togeek.netty.exception;

public class TransportException extends BaseException {
   private static final long serialVersionUID = 1L;

   public TransportException(Throwable cause) {
      super(cause);
   }

   public TransportException(String msg, Throwable cause) {
      super(msg, cause);
   }
}
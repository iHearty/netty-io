package cn.togeek.netty.handler;

public class TransportException extends Exception {
   private static final long serialVersionUID = 1L;

   public TransportException(String msg, Throwable cause) {
      super(msg, cause);
   }
}
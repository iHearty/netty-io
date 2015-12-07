package cn.togeek.netty.exception;

public class TransportSerializationException extends TransportException {
   private static final long serialVersionUID = 1L;

   public TransportSerializationException(String msg, Throwable cause) {
      super(msg, cause);
   }
}
package cn.togeek.netty.exception;

public class RemoteTransportException extends TransportException {
   private static final long serialVersionUID = 1L;

   public RemoteTransportException(String action, Throwable cause) {
      super(action, cause);
   }
}
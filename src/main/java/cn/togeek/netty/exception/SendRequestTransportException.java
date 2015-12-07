package cn.togeek.netty.exception;

public class SendRequestTransportException extends ActionTransportException {
   private static final long serialVersionUID = 1L;

   public SendRequestTransportException(String action, Throwable cause) {
      super(action, cause);
   }
}
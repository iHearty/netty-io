package cn.togeek.netty.handler;

public class SendRequestTransportException extends TransportException {
   private static final long serialVersionUID = 1L;

   public SendRequestTransportException(String action, Throwable cause) {
      super(buildMessage(action), cause);
   }

   private static String buildMessage(String action) {
      StringBuilder sb = new StringBuilder();

      if(action != null) {
         sb.append('[').append(action).append(']');
      }

      return sb.toString();
   }
}
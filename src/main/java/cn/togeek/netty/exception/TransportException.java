package cn.togeek.netty.exception;

public class TransportException extends Exception {
   private static final long serialVersionUID = 1L;

   public TransportException(String action, Throwable cause) {
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
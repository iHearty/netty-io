package cn.togeek.netty.exception;

public class ActionTransportException extends TransportException {
   private static final long serialVersionUID = 1L;

   private final String action;

   public ActionTransportException(String name,
                                   String action,
                                   Throwable cause)
   {
      this(name, action, null, cause);
   }

   public ActionTransportException(String name,
                                   String action,
                                   String msg,
                                   Throwable cause)
   {
      super(buildMessage(name, action, msg), cause);
      this.action = action;
   }

   public String action() {
      return action;
   }

   private static String buildMessage(String name, String action, String msg) {
      StringBuilder sb = new StringBuilder();

      if(name != null) {
         sb.append('[').append(name).append(']');
      }

      if(action != null) {
         sb.append('[').append(action).append(']');
      }

      if(msg != null) {
         sb.append(" ").append(msg);
      }

      return sb.toString();
   }
}
package cn.togeek.netty.handler;

public class RequestHolder<Response extends TransportResponse> {
   private final TransportResponseHandler<Response> handler;

   private final String action;

   RequestHolder(TransportResponseHandler<Response> handler, String action) {
      this.handler = handler;
      this.action = action;
   }

   public TransportResponseHandler<Response> handler() {
      return handler;
   }

   public String action() {
      return action;
   }
}
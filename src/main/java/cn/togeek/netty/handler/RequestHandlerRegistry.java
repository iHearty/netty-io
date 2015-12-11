package cn.togeek.netty.handler;

public class RequestHandlerRegistry<Request extends TransportRequest> {
   private final String action;

   private final String executor;

   private final Request request;

   private final TransportRequestHandler<Request> handler;

   public RequestHandlerRegistry(String action,
                                 String executor,
                                 Request request,
                                 TransportRequestHandler<Request> handler)
   {
      this.action = action;
      this.executor = executor;
      this.request = request;
      this.handler = handler;
   }

   public String getAction() {
      return action;
   }

   public String getExecutor() {
      return executor;
   }

   public Request getRequest() throws Exception {
      return request;
   }

   public TransportRequestHandler<Request> getHandler() {
      return handler;
   }
}
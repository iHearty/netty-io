package cn.togeek.netty.handler;

public interface TransportRequestHandler<Request extends TransportRequest> {
   Request newRequest();

   void handle(Request request, TransportChannel channel) throws Exception;
}
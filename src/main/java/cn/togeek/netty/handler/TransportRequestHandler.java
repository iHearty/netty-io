package cn.togeek.netty.handler;

public interface TransportRequestHandler<Request extends TransportRequest> {
   Request newInstance();

   void handle(Request request, TransportChannel channel) throws Exception;
}
package cn.togeek.netty.handler;

public interface TransportRequestHandler<Request extends TransportRequest> {
   void handle(Request request, TransportChannel channel) throws Exception;
}
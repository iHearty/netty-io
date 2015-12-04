package cn.togeek.netty.handler;

import io.netty.channel.Channel;

public interface TransportRequestHandler<Request extends TransportRequest> {
   void handle(Request request, Channel channel) throws Exception;
}
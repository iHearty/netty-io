package cn.togeek.netty.handler;

import cn.togeek.netty.handler.TransportService.TimeoutHandler;

import io.netty.channel.ChannelId;

public class RequestHolder<Response extends TransportResponse> {
   private final ChannelId channelId;

   private final TransportResponseHandler<Response> handler;

   private final String action;

   private final TimeoutHandler timeoutHandler;

   RequestHolder(ChannelId channelId,
                 TransportResponseHandler<Response> handler,
                 String action,
                 TimeoutHandler timeoutHandler)
   {
      this.channelId = channelId;
      this.handler = handler;
      this.action = action;
      this.timeoutHandler = timeoutHandler;
   }

   public ChannelId channelId() {
      return channelId;
   }

   public TransportResponseHandler<Response> handler() {
      return handler;
   }

   public String action() {
      return action;
   }

   public void cancelTimeout() {
      if(timeoutHandler != null) {
         timeoutHandler.cancel();
      }
   }
}
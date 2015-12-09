package cn.togeek.netty.exception;

import io.netty.channel.ChannelId;

public class ReceiveTimeoutTransportException extends ActionTransportException {
   private static final long serialVersionUID = 1L;

   public ReceiveTimeoutTransportException(ChannelId channelId,
                                           String action,
                                           String msg)
   {
      super(channelId.asShortText(), action, msg, null);
   }
}
package cn.togeek.netty.handler;

import java.io.IOException;

import com.google.protobuf.ByteString;

import cn.togeek.netty.rpc.Transport.Message;
import cn.togeek.netty.rpc.TransportType;

import io.netty.channel.Channel;

public class NettyTransportChannel implements TransportChannel {
   private final String messageId;

   private final String action;

   private final Channel channel;

   public NettyTransportChannel(String messageId,
                                String action,
                                Channel channel)
   {
      this.messageId = messageId;
      this.action = action;
      this.channel = channel;
   }

   @Override
   public String action() {
      return this.action;
   }

   @Override
   public void sendResponse(TransportResponse response) throws IOException {
      ByteString message = response.writeTo();

      Message.Builder builder = Message.newBuilder()
         .setId(messageId)
         .setType(TransportType.RESPONSE.getType())
         .setAction(action)
         .setMessage(message);
      channel.writeAndFlush(builder.build());
   }
}
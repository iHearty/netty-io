package cn.togeek.netty.handler;

import java.io.IOException;

import com.google.protobuf.ByteString;

import cn.togeek.netty.rpc.Transport.Message;
import cn.togeek.netty.rpc.TransportStatus;

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
      final ByteString message = response.writeTo();
      int status = 0;
      status = TransportStatus.setResponse(status);

      Message.Builder builder = Message.newBuilder()
         .setId(messageId)
         .setStatus(status)
         .setAction(action)
         .setMessage(message);
      channel.writeAndFlush(builder.build());
   }

   @Override
   public void sendResponse(Throwable error) throws IOException {
      int status = 0;
      status = TransportStatus.setResponse(status);
      status = TransportStatus.setError(status);

      // TODO write throwable
      Message.Builder builder = Message.newBuilder()
         .setId(messageId)
         .setStatus(status)
         .setAction(action)
         .setMessage(ByteString.EMPTY);
      channel.writeAndFlush(builder.build());
   }
}
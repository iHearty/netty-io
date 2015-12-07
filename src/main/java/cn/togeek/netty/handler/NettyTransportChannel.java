package cn.togeek.netty.handler;

import java.io.IOException;

import com.google.protobuf.ByteString;

import cn.togeek.netty.rpc.Transport.Message;
import cn.togeek.netty.rpc.TransportStatus;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;

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
      final ByteBuf output = response.writeTo();

      try {
         int status = 0;
         status = TransportStatus.setResponse(status);

         Message.Builder builder = Message.newBuilder()
            .setId(messageId)
            .setStatus(status)
            .setAction(action)
            .setMessage(ByteString.copyFrom(output.nioBuffer()));
         channel.writeAndFlush(builder.build());
      }
      finally {
         ReferenceCountUtil.release(output);
      }
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
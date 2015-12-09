package cn.togeek.netty.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ByteString;

import cn.togeek.netty.rpc.Transport.Message;
import cn.togeek.netty.rpc.TransportStatus;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class HeartbeatHandler extends SimpleChannelInboundHandler<Message> {
   private int status;

   private volatile ScheduledFuture<?> heartBeat;

   public HeartbeatHandler(int status) {
      this.status = TransportStatus.setHeartbate(status);
   }

   @Override
   public void channelActive(final ChannelHandlerContext context)
      throws Exception
   {
      if(TransportStatus.isRequest(status)) {
         Message heartbeat = buildMessage(TransportStatus.setResponse(status),
            Unpooled.buffer());
         context.writeAndFlush(heartbeat);
      }

      super.channelActive(context);
   }

   @Override
   protected void messageReceived(final ChannelHandlerContext context,
                                  Message message)
                                     throws Exception
   {
      if(TransportStatus.isHeartbate(message.getStatus())) {
         if(TransportStatus.isRequest(message.getStatus())) {
            heartBeat = context.executor().schedule(
               new Runnable() {
                  @Override
                  public void run() {
                     Message heartbeat = buildMessage(
                        TransportStatus.setResponse(status),
                        Unpooled.buffer());
                     context.writeAndFlush(heartbeat);
                  }
               }, 5000,
               TimeUnit.MILLISECONDS);
         }
         else {
            Message heartbeat = buildMessage(TransportStatus.setRequest(status),
               Unpooled.buffer());
            context.writeAndFlush(heartbeat);
         }
      }
      else {
         context.fireChannelRead(message);
      }
   }

   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
      throws Exception
   {
      if(heartBeat != null) {
         heartBeat.cancel(true);
         heartBeat = null;
      }

      ctx.fireExceptionCaught(cause);
   }

   private Message buildMessage(int status, ByteBuf message) {
      Message.Builder builder = Message.newBuilder()
         .setStatus(status)
         .setMessage(ByteString.copyFrom(message.nioBuffer()));
      return builder.build();
   }
}
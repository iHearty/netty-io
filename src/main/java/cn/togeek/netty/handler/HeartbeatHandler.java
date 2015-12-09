package cn.togeek.netty.handler;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ByteString;

import cn.togeek.netty.Settings;
import cn.togeek.netty.TransportBase;
import cn.togeek.netty.exception.SettingsException;
import cn.togeek.netty.rpc.Transport.Message;
import cn.togeek.netty.rpc.TransportStatus;
import cn.togeek.netty.util.ByteBufs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

public class HeartbeatHandler extends SimpleChannelInboundHandler<Message> {
   private int status;

   private int period = 5000;

   private Settings props;

   private String prefix = "heartbeat";

   private volatile ScheduledFuture<?> heartbeat;

   public HeartbeatHandler(Settings settings, int status) {
      this.status = TransportStatus.setHeartbate(status);
      this.props = settings.getAsSettings(prefix);

      try {
         this.period = settings.getAsInt(TransportBase.HEARTBEAT_PERIOD, 5000);
      }
      catch(SettingsException e) {
      }
   }

   @Override
   public void channelActive(final ChannelHandlerContext context)
      throws Exception
   {
      if(TransportStatus.isRequest(status)) {
         final ByteBuf out = Unpooled.buffer();

         try {
            writeProps(out);
            Message heartbeat =
               buildMessage(TransportStatus.setResponse(status), out);
            context.writeAndFlush(heartbeat);
         }
         finally {
            ReferenceCountUtil.release(out);
         }
      }

      super.channelActive(context);
   }

   @Override
   protected void messageReceived(final ChannelHandlerContext context,
                                  Message message)
                                     throws Exception
   {
      if(TransportStatus.isHeartbate(message.getStatus())) {
         // client
         if(TransportStatus.isRequest(message.getStatus())) {
            heartbeat = context.executor().schedule(
               new Runnable() {
                  @Override
                  public void run() {
                     Message heartbeat = buildMessage(
                        TransportStatus.setResponse(status));
                     context.writeAndFlush(heartbeat);
                  }
               }, period,
               TimeUnit.MILLISECONDS);
         }
         // server
         else {
            ByteBuf in = null;

            try {
               if(!message.getMessage().isEmpty()) {
                  in = Unpooled
                     .copiedBuffer(message.getMessage().asReadOnlyByteBuffer());
                  readProps(in);
               }

               Message heartbeat =
                  buildMessage(TransportStatus.setRequest(status));
               context.writeAndFlush(heartbeat);
            }
            finally {
               ReferenceCountUtil.release(in);
            }
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
      if(heartbeat != null) {
         heartbeat.cancel(true);
         heartbeat = null;
      }

      ctx.fireExceptionCaught(cause);
   }

   @Override
   public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
      throws Exception
   {
      if(evt instanceof IdleStateEvent) {
         IdleStateEvent event = (IdleStateEvent) evt;
         System.out.println(" state === " + event.state());
      }

      super.userEventTriggered(ctx, evt);
   }

   private void writeProps(ByteBuf out) {
      Map<String, String> map = props.getAsMap();
      out.writeInt(map.size());

      for(String key : map.keySet()) {
         ByteBufs.writeString(key, out);
         ByteBufs.writeString(map.get(key), out);
      }
   }

   private void readProps(ByteBuf in) {
      int size = in.readInt();

      Settings.Builder builder = Settings.builder().put(props);

      for(int i = 0; i < size; i++) {
         builder.put(ByteBufs.readString(in), ByteBufs.readString(in));
      }

      props = builder.build();
   }

   private Message buildMessage(int status) {
      return buildMessage(status, null);
   }

   private Message buildMessage(int status, ByteBuf message) {
      Message.Builder builder = Message.newBuilder().setStatus(status);

      if(message != null) {
         builder.setMessage(ByteString.copyFrom(message.nioBuffer()));
      }

      return builder.build();
   }
}
package cn.togeek.netty.server;

import java.util.concurrent.TimeUnit;

import com.google.protobuf.MessageLite;

import cn.togeek.netty.Settings;
import cn.togeek.netty.handler.ChannelRegistryHandler;
import cn.togeek.netty.handler.HeartbeatHandler;
import cn.togeek.netty.handler.MessageHandler;
import cn.togeek.netty.rpc.TransportStatus;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

public class ServerChannelInitializer
   extends ChannelInitializer<SocketChannel>
{
   private final Settings settings;

   private final MessageLite defaultInstance;

   ServerChannelInitializer(Settings settings, MessageLite defaultInstance) {
      this.settings = settings;
      this.defaultInstance = defaultInstance;
   }

   @Override
   protected void initChannel(SocketChannel channel) throws Exception {
      ChannelPipeline pipeline = channel.pipeline();
      pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
      pipeline.addLast("protobufDecoder", new ProtobufDecoder(defaultInstance));
      pipeline.addLast("frameEncoder",
         new ProtobufVarint32LengthFieldPrepender());
      pipeline.addLast("protobufEncoder", new ProtobufEncoder());
      pipeline.addLast("registry", new ChannelRegistryHandler());
      pipeline.addLast("idle", new IdleStateHandler(0,
         0, 60, TimeUnit.SECONDS));
      pipeline.addLast("heartbeat",
         new HeartbeatHandler(settings, TransportStatus.setResponse(0)));
      pipeline.addLast("handler", new MessageHandler());
   }
}
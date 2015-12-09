package cn.togeek.netty.client;

import com.google.protobuf.MessageLite;

import cn.togeek.netty.Settings;
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

public class ClientChannelInitializer
   extends ChannelInitializer<SocketChannel>
{
   private final Settings settings;

   private final MessageLite defaultInstance;

   ClientChannelInitializer(Settings settings, MessageLite defaultInstance) {
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
      pipeline.addLast("heartbeat",
         new HeartbeatHandler(settings, TransportStatus.setRequest(0)));
      pipeline.addLast("handler", new MessageHandler());
   }
}
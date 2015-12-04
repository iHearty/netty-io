package cn.togeek.netty.client;

import com.google.protobuf.MessageLite;

import cn.togeek.netty.handler.TransportHandler;

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
   private final MessageLite defaultInstance;

   ClientChannelInitializer(MessageLite defaultInstance) {
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
      pipeline.addLast("handler", new TransportHandler());
   }
}
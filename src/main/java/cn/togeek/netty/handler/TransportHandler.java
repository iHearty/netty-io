package cn.togeek.netty.handler;

import java.io.IOException;

import com.google.protobuf.ByteString;

import cn.togeek.netty.rpc.Transport.Message;
import cn.togeek.netty.rpc.TransportType;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TransportHandler extends SimpleChannelInboundHandler<Message> {
   @Override
   @SuppressWarnings({"rawtypes"})
   protected void messageReceived(ChannelHandlerContext context,
                                  Message message) throws Exception
   {
      if(TransportType.isRequset(message.getType())) {
         handleRequest(context.channel(), message.getId(), message.getAction(),
            message.getMessage());
      }
      else if(TransportType.isResponse(message.getType())) {
         TransportResponseHandler handler =
            TransportService.INSTANCE.onResponseReceived(message.getId());

         if(handler != null) {
            handleResponse(context.channel(), message.getMessage(), handler);
         }
      }

      new UnsupportedOperationException(
         "wrong message type [" + message.getType() + "]");
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   private void handleRequest(Channel channel,
                              String messageId,
                              String action,
                              ByteString message) throws IOException
   {
      final RequestHandlerRegistry registry =
         TransportService.INSTANCE.getRequestHandler(action);
      final NettyTransportChannel transportChannel =
         new NettyTransportChannel(messageId, action, channel);

      try {
         TransportRequest request = registry.newRequest();
         request.readFrom(message);
         registry.getHandler().handle(request, transportChannel);
      }
      catch(IOException e) {
         throw e;
      }
      catch(Exception e) {
         // TODO send error response
      }
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   private void handleResponse(Channel channel,
                               ByteString message,
                               TransportResponseHandler handler)
   {
      final TransportResponse response = handler.newInstance();

      try {
         response.readFrom(message);
         handler.handleResponse(response);
      }
      catch(IOException e) {
         // TODO send error response
      }
   }
}
package cn.togeek.netty.handler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;

import cn.togeek.netty.exception.RemoteTransportException;
import cn.togeek.netty.rpc.Transport.Message;
import cn.togeek.netty.rpc.TransportStatus;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TransportHandler extends SimpleChannelInboundHandler<Message> {
   private static final Logger logger = Logger
      .getLogger(TransportHandler.class.getName());

   @Override
   @SuppressWarnings({"rawtypes"})
   protected void messageReceived(ChannelHandlerContext context,
                                  Message message) throws Exception
   {
      if(TransportStatus.isRequest(message.getStatus())) {
         handleRequest(context.channel(), message.getId(), message.getAction(),
            message.getMessage());
      }
      else {
         TransportResponseHandler handler =
            TransportService.INSTANCE.onResponseReceived(message.getId());

         if(handler != null) {
            if(TransportStatus.isError(message.getStatus())) {
               // TODO read message to throwable
               handleException(handler, null);
            }
            else {
               handleResponse(context.channel(), message.getMessage(), handler);
            }
         }
      }
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   private void handleRequest(Channel channel,
                              String messageId,
                              String action,
                              ByteString message)
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
      catch(Throwable e) {
         try {
            transportChannel.sendResponse(e);
         }
         catch(IOException ex) {
            logger.log(Level.SEVERE,
               "Failed to send error message back to client for action ["
                  + action + "]",
               e);
            logger.log(Level.SEVERE, "Actual Exception", ex);
         }
      }
   }

   @SuppressWarnings("rawtypes")
   private void handleException(TransportResponseHandler handler,
                                Throwable error)
   {
      if(!(error instanceof RemoteTransportException)) {
         error = new RemoteTransportException(error.getMessage(), error);
      }

      final RemoteTransportException rtx = (RemoteTransportException) error;
      handler.handleException(rtx);
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
      catch(Throwable e) {
         // TODO send error response
      }
   }
}
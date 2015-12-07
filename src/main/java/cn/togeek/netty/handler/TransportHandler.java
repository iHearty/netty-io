package cn.togeek.netty.handler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;

import cn.togeek.netty.exception.Exceptions;
import cn.togeek.netty.exception.RemoteTransportException;
import cn.togeek.netty.exception.ResponseHandlerFailureTransportException;
import cn.togeek.netty.exception.TransportSerializationException;
import cn.togeek.netty.rpc.Transport.Message;
import cn.togeek.netty.rpc.TransportStatus;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

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
               Throwable error;
               ByteBuf input = Unpooled
                  .copiedBuffer(message.getMessage().asReadOnlyByteBuffer());

               try {
                  error = Exceptions.readThrowable(input);
               }
               catch(Throwable e) {
                  error = new TransportSerializationException(
                     "Failed to deserialize exception response from stream", e);
               }
               finally {
                  ReferenceCountUtil.release(input);
               }

               handleException(handler, error);
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
      ByteBuf input = null;

      try {
         TransportRequest request = registry.newRequest();
         input = Unpooled.copiedBuffer(message.asReadOnlyByteBuffer());
         request.readFrom(input);
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
      finally {
         ReferenceCountUtil.release(input);
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
      ByteBuf input = null;

      try {
         input = Unpooled.copiedBuffer(message.asReadOnlyByteBuffer());
         response.readFrom(input);
      }
      catch(Throwable e) {
         handleException(handler, new TransportSerializationException(
            "Failed to deserialize response of type ["
               + response.getClass().getName() + "]",
            e));
         return;
      }
      finally {
         ReferenceCountUtil.release(input);
      }

      try {
         handler.handleResponse(response);
      }
      catch(Throwable e) {
         handleException(handler,
            new ResponseHandlerFailureTransportException(e));
      }
   }
}
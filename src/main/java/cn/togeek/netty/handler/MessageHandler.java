package cn.togeek.netty.handler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.ByteString;

import cn.togeek.netty.concurrent.AbstractRunnable;
import cn.togeek.netty.concurrent.ThreadPool;
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

public class MessageHandler extends SimpleChannelInboundHandler<Message> {
   private static final Logger logger = Logger
      .getLogger(MessageHandler.class.getName());

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

   @SuppressWarnings({"rawtypes"})
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

         if(ThreadPool.Names.SAME.equals(registry.getExecutor())) {
            channel.eventLoop().execute(
               new RequestHandler(registry, request, transportChannel));
         }
         else {
            ThreadPool.INSTANCE.executor(
               registry.getExecutor()).execute(
                  new RequestHandler(registry, request, transportChannel));
         }
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
   private void handleException(final TransportResponseHandler handler,
                                Throwable error)
   {
      if(!(error instanceof RemoteTransportException)) {
         error = new RemoteTransportException(error.getMessage(), error);
      }

      final RemoteTransportException rtx = (RemoteTransportException) error;
      final Runnable run = new Runnable() {
         @Override
         public void run() {
            try {
               handler.handleException(rtx);
            }
            catch(Throwable e) {
               logger.log(Level.SEVERE,
                  "failed to handle exception response [" + e.getMessage()
                     + "]",
                  e);
            }
         }
      };

      if(ThreadPool.Names.SAME.equals(handler.executor())) {
         handler.handleException(rtx);
      }
      else {
         ThreadPool.INSTANCE.executor(handler.executor()).execute(run);
      }
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
         try {
            ResponseHandler resHandler = new ResponseHandler(handler, response);

            if(ThreadPool.Names.SAME.equals(handler.executor())) {
               channel.eventLoop().execute(resHandler);
            }
            else {
               ThreadPool.INSTANCE.executor(handler.executor())
                  .execute(resHandler);
            }
         }
         catch(Throwable e) {
            handleException(handler,
               new ResponseHandlerFailureTransportException(e));
         }
      }
      catch(Throwable e) {
         handleException(handler,
            new ResponseHandlerFailureTransportException(e));
      }
   }

   class ResponseHandler implements Runnable {
      private final TransportResponseHandler handler;

      private final TransportResponse response;

      public ResponseHandler(TransportResponseHandler handler,
                             TransportResponse response)
      {
         this.handler = handler;
         this.response = response;
      }

      @SuppressWarnings({"unchecked"})
      @Override
      public void run() {
         try {
            handler.handleResponse(response);
         }
         catch(Throwable e) {
            handleException(handler,
               new ResponseHandlerFailureTransportException(e));
         }
      }
   }

   class RequestHandler extends AbstractRunnable {
      private final RequestHandlerRegistry registry;

      private final TransportRequest request;

      private final NettyTransportChannel transportChannel;

      public RequestHandler(RequestHandlerRegistry registry,
                            TransportRequest request,
                            NettyTransportChannel transportChannel)
      {
         this.registry = registry;
         this.request = request;
         this.transportChannel = transportChannel;
      }

      @SuppressWarnings({"unchecked"})
      @Override
      protected void doRun() throws Exception {
         registry.getHandler().handle(request, transportChannel);
      }

      @Override
      public void onFailure(Throwable e) {
         // we can only send a response transport is started....
         try {
            transportChannel.sendResponse(e);
         }
         catch(Throwable ex) {
            logger.log(Level.WARNING,
               "Failed to send error message back to client for action ["
                  + registry.getAction() + "]",
               ex);
            logger.log(Level.WARNING, "Actual Exception", e);
         }
      }
   }
}
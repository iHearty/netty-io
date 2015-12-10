package cn.togeek.netty.handler;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

import com.google.protobuf.ByteString;

import cn.togeek.netty.concurrent.ThreadPool;
import cn.togeek.netty.exception.ReceiveTimeoutTransportException;
import cn.togeek.netty.exception.SendRequestTransportException;
import cn.togeek.netty.rpc.Transport.Message;
import cn.togeek.netty.rpc.TransportStatus;
import cn.togeek.netty.util.Strings;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.PlatformDependent;

public class TransportService {
   public static final TransportService INSTANCE = new TransportService();

   private final ConcurrentMap<String, RequestHandlerRegistry> requestHandlers =
      PlatformDependent.newConcurrentHashMap();

   private final ConcurrentMap<String, RequestHolder> requestHolders =
      PlatformDependent.newConcurrentHashMap();

   // An LRU (don't really care about concurrency here) that holds the latest
   // timed out requests so if they do show up, we can print more descriptive
   // information about them
   final Map<String, TimeoutInfoHolder> timeoutInfoHandlers =
      Collections.synchronizedMap(
         new LinkedHashMap<String, TimeoutInfoHolder>(100, .75F, true) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
               return size() > 100;
            }
         });

   public <Request extends TransportRequest> void
      registerRequestHandler(String action,
                             String executor,
                             Class<Request> request,
                             TransportRequestHandler<Request> handler)
   {
      RequestHandlerRegistry<Request> registry =
         new RequestHandlerRegistry<>(action, executor, request, handler);
      requestHandlers.put(action, registry);
   }

   public RequestHandlerRegistry<?> getRequestHandler(String action) {
      return requestHandlers.get(action);
   }

   public TransportResponseHandler<?>
      onResponseReceived(final String messageId)
   {
      RequestHolder<?> holder = requestHolders.remove(messageId);
      if(holder == null) {
         timeoutInfoHandlers.remove(messageId);
         return null;
      }

      holder.cancelTimeout();
      return holder.handler();
   }

   public <Response extends TransportResponse> void
      sendRequest(final Node node,
                  final String action,
                  final TransportRequest request,
                  TransportResponseHandler<Response> handler)
                     throws IOException
   {
      sendRequest(node, action, request, null, handler);
   }

   public <Response extends TransportResponse> void
      sendRequest(final Node node,
                  final String action,
                  final TransportRequest request,
                  final Integer timeout,
                  TransportResponseHandler<Response> handler)
                     throws IOException
   {
      final String messageId = Strings.randomBase64UUID();
      final ByteBuf output = request.writeTo();
      final TimeoutHandler timeoutHandler;

      try {
         int status = 0;
         status = TransportStatus.setRequest(status);

         Message.Builder builder = Message.newBuilder()
            .setId(messageId)
            .setStatus(status)
            .setAction(action)
            .setMessage(ByteString.copyFrom(output.nioBuffer()));

         if(timeout == null) {
            timeoutHandler = null;
         }
         else {
            timeoutHandler = new TimeoutHandler(messageId);
         }

         if(timeoutHandler != null) {
            timeoutHandler.future = ThreadPool.INSTANCE.schedule(timeout,
               ThreadPool.Names.GENERIC, timeoutHandler);
         }

         requestHolders.put(messageId,
            new RequestHolder<>(node, handler, action, timeoutHandler));
         node.writeAndFlush(builder.build());
      }
      catch(Throwable e) {
         final RequestHolder<?> holder = requestHolders.remove(messageId);

         if(holder != null) {
            holder.cancelTimeout();
            final SendRequestTransportException sendRequestException =
               new SendRequestTransportException(action, e);

            ThreadPool.INSTANCE.generic().execute(new Runnable() {
               @Override
               public void run() {
                  holder.handler().handleException(sendRequestException);
               }
            });
         }
      }
      finally {
         ReferenceCountUtil.release(output);
      }
   }

   class TimeoutHandler implements Runnable {
      private final String messageId;

      private final long sentTime = System.currentTimeMillis();

      volatile ScheduledFuture<?> future;

      TimeoutHandler(String messageId) {
         this.messageId = messageId;
      }

      @Override
      public void run() {
         // we get first to make sure we only add the TimeoutInfoHandler if
         // needed.
         final RequestHolder<?> holder = requestHolders.get(messageId);

         if(holder != null) {
            // add it to the timeout information holder, in case we are going to
            // get a response later
            long timeoutTime = System.currentTimeMillis();

            timeoutInfoHandlers.put(messageId, new TimeoutInfoHolder(
               holder.node(), holder.action(), sentTime, timeoutTime));
            // now that we have the information visible via timeoutInfoHandlers,
            // we try to remove the request id
            final RequestHolder<?> removedHolder =
               requestHolders.remove(messageId);

            if(removedHolder != null) {
               removedHolder.handler()
                  .handleException(new ReceiveTimeoutTransportException(
                     holder.node(), holder.action(),
                     "message_id [" + messageId + "] timed out after ["
                        + (timeoutTime - sentTime) + "ms]"));
            }
            else {
               // response was processed, remove timeout info.
               timeoutInfoHandlers.remove(messageId);
            }
         }
      }

      /**
       * cancels timeout handling. this is a best effort only to avoid running
       * it. remove the requestId from {@link #clientHandlers}
       * to make sure this doesn't run.
       */
      public void cancel() {
         if(future != null) {
            // this method is a forbidden API since it interrupts threads
            future.cancel(false);
         }
      }
   }

   static class TimeoutInfoHolder {
      private final Node node;

      private final String action;

      private final long sentTime;

      private final long timeoutTime;

      TimeoutInfoHolder(Node node,
                        String action,
                        long sentTime,
                        long timeoutTime)
      {
         this.node = node;
         this.action = action;
         this.sentTime = sentTime;
         this.timeoutTime = timeoutTime;
      }

      public Node node() {
         return node;
      }

      public String action() {
         return action;
      }

      public long sentTime() {
         return sentTime;
      }

      public long timeoutTime() {
         return timeoutTime;
      }
   }

   private TransportService() {
   }
}
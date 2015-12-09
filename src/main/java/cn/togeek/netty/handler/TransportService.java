package cn.togeek.netty.handler;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import com.google.protobuf.ByteString;

import cn.togeek.netty.exception.SendRequestTransportException;
import cn.togeek.netty.rpc.Transport.Message;
import cn.togeek.netty.rpc.TransportStatus;
import cn.togeek.netty.util.Strings;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.PlatformDependent;

public class TransportService {
   public static final TransportService INSTANCE = new TransportService();

   private final ChannelGroup channels = new DefaultChannelGroup(
      GlobalEventExecutor.INSTANCE);

   private final ConcurrentMap<String, RequestHandlerRegistry> requestHandlers =
      PlatformDependent.newConcurrentHashMap();

   private final ConcurrentMap<String, RequestHolder> requestHolders =
      PlatformDependent.newConcurrentHashMap();

   public void addChannel(Channel channel) {
      channels.add(channel);
   }

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
      return holder.handler();
   }

   public <Response extends TransportResponse> void
      sendRequest(final ChannelId channelId,
                  final String action,
                  final TransportRequest request,
                  TransportResponseHandler<Response> handler)
                     throws IOException
   {
      final String messageId = Strings.randomBase64UUID();
      final ByteBuf output = request.writeTo();

      try {
         int status = 0;
         status = TransportStatus.setRequest(status);

         Message.Builder builder = Message.newBuilder()
            .setId(messageId)
            .setStatus(status)
            .setAction(action)
            .setMessage(ByteString.copyFrom(output.nioBuffer()));
         requestHolders.put(messageId, new RequestHolder<>(handler, action));
         channels.find(channelId).writeAndFlush(builder.build());
      }
      catch(Throwable e) {
         final RequestHolder<?> holder = requestHolders.remove(messageId);

         if(holder != null) {
            final SendRequestTransportException sendRequestException =
               new SendRequestTransportException(action, e);

            channels.find(channelId).eventLoop().execute(new Runnable() {
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

   private TransportService() {
   }
}
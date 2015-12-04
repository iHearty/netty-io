package cn.togeek.netty.handler;

import java.util.concurrent.ConcurrentMap;

import com.google.protobuf.ByteString;

import cn.togeek.netty.rpc.Transport.Message;
import cn.togeek.netty.rpc.TransportType;
import cn.togeek.netty.util.Strings;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.PlatformDependent;

public class TransportService {
   private static final ChannelGroup channels = new DefaultChannelGroup(
      GlobalEventExecutor.INSTANCE);

   private static final ConcurrentMap<String, RequestHandlerRegistry> requestHandlers =
      PlatformDependent.newConcurrentHashMap();

   private static final ConcurrentMap<String, RequestHolder> requestHolders =
      PlatformDependent.newConcurrentHashMap();

   public static void addChannel(Channel channel) {
      channels.add(channel);
   }

   public static <Request extends TransportRequest> void
      registerRequestHandler(String action,
                             Class<Request> request,
                             TransportRequestHandler<Request> handler)
   {
      RequestHandlerRegistry<Request> registry =
         new RequestHandlerRegistry<>(action, request, handler);
      requestHandlers.put(action, registry);
   }

   public static RequestHandlerRegistry getRequestHandler(String action) {
      return requestHandlers.get(action);
   }

   public static TransportResponseHandler
      onResponseReceived(final String messageId)
   {
      RequestHolder holder = requestHolders.remove(messageId);
      return holder.handler();
   }

   public static <Response extends TransportResponse> void
      sendRequest(final ChannelId channelId,
                  final String action,
                  byte[] message,
                  TransportResponseHandler<Response> handler)
   {
      final String messageId = Strings.randomBase64UUID();
      Message.Builder builder = Message.newBuilder()
         .setId(messageId)
         .setType(TransportType.REQUEST.getType())
         .setAction(action)
         .setMessage(ByteString.copyFrom(message));

      requestHolders.put(messageId, new RequestHolder<>(handler, action));
      channels.find(channelId).writeAndFlush(builder.build());
   }

   private TransportService() {
   }
}
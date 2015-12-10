package cn.togeek.netty.handler;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import cn.togeek.netty.rpc.Transport.Message;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelMatchers;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.ConcurrentSet;

public class NodeService {
   public static final NodeService INSTANCE = new NodeService();

   private final ChannelFutureListener REMOVE = new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
         deregister(future.channel());
      }
   };

   private final ChannelGroup channels = new DefaultChannelGroup(
      GlobalEventExecutor.INSTANCE);

   private final Set<Node> nodes = new ConcurrentSet<>();

   public Set<Node> nodes() {
      return Collections.unmodifiableSet(nodes);
   }

   public ChannelGroupFuture writeAndFlush(Message message) {
      return channels.writeAndFlush(message, ChannelMatchers.all());
   }

   public void register(Channel channel) {
      boolean added = channels.add(channel);

      if(added) {
         nodes.add(new Node(channel));
         channel.closeFuture().addListener(REMOVE);
      }
   }

   public void deregister(Channel channel) {
      for(Node node : nodes) {
         if(node.channel().id() == channel.id()) {
            nodes.remove(node);
         }
      }
   }

   public <T> Node find(T o) {
      for(Node node : nodes) {
         if(node.match(o)) {
            return node;
         }
      }

      return null;
   }

   public void update(Channel channel, Map<String, String> props) {
      Node node = find(channel);

      if(node != null) {
         node.update(props);
      }
   }
}
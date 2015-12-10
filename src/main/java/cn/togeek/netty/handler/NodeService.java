package cn.togeek.netty.handler;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import cn.togeek.netty.Settings;
import cn.togeek.netty.util.Strings;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.ConcurrentSet;

public class NodeService {
   public static final NodeService INSTANCE = new NodeService();

   public static final String NODE_CLASS = "NODE_CLASS";

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

   public void register(Settings settings, Channel channel) {
      boolean added = channels.add(channel);

      if(added) {
         Node node;
         String className = settings.get(NODE_CLASS);

         if(Strings.isEmpty(className)) {
            node = new Node(channel);
         }
         else {
            try {
               Class<Node> clazz = (Class<Node>) Class.forName(className);
               Constructor<Node> constructor =
                  clazz.getConstructor(Channel.class);
               node = constructor.newInstance(channel);
            }
            catch(Exception e) {
               node = new Node(channel);
            }
         }

         nodes.add(node);
         channel.closeFuture().addListener(REMOVE);
      }
   }

   public void deregister(Channel channel) {
      Node node = find(channel);

      if(node != null) {
         nodes.remove(node);
      }
   }

   public Node find(Object o) {
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
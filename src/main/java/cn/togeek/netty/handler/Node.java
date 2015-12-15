package cn.togeek.netty.handler;

import java.util.Map;

import cn.togeek.netty.Settings;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

public class Node {
   private Channel channel;

   private Settings settings;

   public Node() {
      this.settings = Settings.EMPTY;
   }

   public Node(Channel channel) {
      this();
      this.channel = channel;
   }

   // TODO
   public String name() {
      return channel.id().asShortText();
   }

   public Channel channel() {
      return channel;
   }

   public String getProperty(String key) {
      return settings.get(key);
   }

   public void update(Map<String, String> props) {
      this.settings = Settings.builder()
         .put(settings)
         .put(props)
         .build();
   }

   public boolean match(Object o) {
      if(o instanceof ChannelId) {
         return o == channel().id();
      }
      else if(o instanceof Channel) {
         return o == channel();
      }

      return false;
   }

   @Override
   public String toString() {
      return "[" + channel + "][" + settings.getAsMap() + "]";
   }
}
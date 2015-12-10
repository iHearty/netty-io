package cn.togeek.netty.handler;

import java.util.Map;

import cn.togeek.netty.Settings;

import io.netty.channel.Channel;

public abstract class Node {
   private Channel channel;

   private Settings settings;

   public Node(Channel channel) {
      this.channel = channel;
      this.settings = Settings.EMPTY;
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

   public abstract boolean match(Object o);

   @Override
   public String toString() {
      return "[" + channel + "][" + settings.getAsMap() + "]";
   }
}
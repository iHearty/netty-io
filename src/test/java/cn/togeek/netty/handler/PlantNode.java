package cn.togeek.netty.handler;

import cn.togeek.netty.util.Strings;

import io.netty.channel.Channel;

public class PlantNode extends Node {
   public PlantNode(Channel channel) {
      super(channel);
   }

   public Integer getPlantId() {
      if(Strings.isEmpty(getProperty("plantId"))) {
         return null;
      }

      return Integer.parseInt(getProperty("plantId"));
   }

   @Override
   public boolean match(Object o) {
      if(getPlantId() == o) {
         return true;
      }

      return super.match(o);
   }
}
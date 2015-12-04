package cn.togeek.netty;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.Channel;

public abstract class TransportBase<T extends AbstractBootstrap<T, ? extends Channel>> {
   public static final String SO_BACKLOG = "SO_BACKLOG";

   public static final String SO_SNDBUF = "SO_SNDBUF";

   public static final String SO_RCVBUF = "SO_RCVBUF";

   public static final String SERVER_HOST = "SERVER_HOST";

   public static final String SERVER_PORT = "SERVER_PORT";

   protected T bootstrap;

   protected abstract void init(Settings settings) throws SettingsException;

   public abstract void start(Settings settings) throws Exception;

   protected String host(Settings settings) {
      return settings.get(SERVER_HOST);
   }

   protected int port(Settings settings) throws SettingsException {
      return settings.getAsInt(SERVER_PORT, 52400);
   }
}
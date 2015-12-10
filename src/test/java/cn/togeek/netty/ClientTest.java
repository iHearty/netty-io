package cn.togeek.netty;

import cn.togeek.netty.client.TransportClient;
import cn.togeek.netty.concurrent.ThreadPool;
import cn.togeek.netty.exception.SettingsException;
import cn.togeek.netty.handler.TransportService;
import cn.togeek.netty.server.TransportServer;

public class ClientTest {
   public static void main(String[] args) throws SettingsException {
      TransportService.INSTANCE.registerRequestHandler(
         TestAction.class.getName(),
         ThreadPool.Names.GENERIC,
         TestActionRequest.class,
         new TestActionRequestHandler());

      final Settings clientSettings = Settings.builder()
         .put(TransportServer.SERVER_HOST, "0.0.0.0")
         .put(TransportServer.SERVER_PORT, 9090)
         .put(TransportServer.HEARTBEAT_PERIOD, 5000)
         .put("heartbeat.plantId", 15) // client heartbeat properties
         .build();
      final TransportClient client = new TransportClient();
      client.start(clientSettings);
   }
}
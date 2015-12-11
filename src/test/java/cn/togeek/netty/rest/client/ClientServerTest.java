package cn.togeek.netty.rest.client;

import org.restlet.Component;
import org.restlet.data.Protocol;

import cn.togeek.netty.Settings;
import cn.togeek.netty.client.TransportClient;
import cn.togeek.netty.rest.HttpTransportAction;
import cn.togeek.netty.server.TransportServer;

public class ClientServerTest {
   public static void main(String[] args) throws Exception {
      if(args.length == 0) {
         return;
      }

      String serverHost = args[0];
      String plantId = args[1];

      Component component = new Component();
      component.getServers().add(Protocol.HTTP, 52500);

      HelloApplication hello = new HelloApplication();
      component.getDefaultHost().attach(hello);
      component.start();

      // TODO, regist action handler
      new HttpTransportAction();

      final Settings clientSettings = Settings.builder()
         .put(TransportServer.SERVER_HOST, serverHost)
         .put(TransportServer.SERVER_PORT, 52400)
         .put(TransportServer.HEARTBEAT_PERIOD, 5000)
         .put("heartbeat.plantId", plantId) // client heartbeat properties
         .build();
      final TransportClient client = new TransportClient();
      client.start(clientSettings);
   }
}
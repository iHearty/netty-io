package cn.togeek.netty;

import java.util.Observable;
import java.util.Observer;

import cn.togeek.netty.handler.GlobalObservable;
import cn.togeek.netty.handler.NodeService;
import cn.togeek.netty.handler.PlantNode;
import cn.togeek.netty.server.TransportServer;

public class ServerTest {
   public static void main(String[] args) {
      final Settings serverSettings = Settings.builder()
         .put(TransportServer.SERVER_HOST, "0.0.0.0")
         .put(TransportServer.SERVER_PORT, 9090)
         .put(NodeService.NODE_CLASS, PlantNode.class.getName())
         .build();
      final TransportServer server = new TransportServer();

      new Thread() {
         @Override
         public void run() {
            try {
               server.start(serverSettings);
            }
            catch(Exception e) {
            }
         }
      }.start();

      GlobalObservable.INSTANCE.addObserver(GlobalObservable.Types.CONNECTED,
         new Observer() {
            @Override
            public void update(Observable observable, Object arg) {
               System.out.println(NodeService.INSTANCE.nodes());
            }
         });
   }
}
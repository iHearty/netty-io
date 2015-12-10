package cn.togeek.netty;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import cn.togeek.netty.client.TransportClient;
import cn.togeek.netty.concurrent.ThreadPool;
import cn.togeek.netty.exception.SettingsException;
import cn.togeek.netty.handler.GlobalObservable;
import cn.togeek.netty.handler.Node;
import cn.togeek.netty.handler.NodeService;
import cn.togeek.netty.handler.PlantNode;
import cn.togeek.netty.handler.TransportService;
import cn.togeek.netty.server.TransportServer;

public class NettyTest {
   private static final Logger logger = Logger
      .getLogger(NettyTest.class.getName());

   public static void main(String[] args) throws SettingsException {
      TransportService.INSTANCE.registerRequestHandler(
         TestAction.class.getName(),
         ThreadPool.Names.GENERIC,
         TestActionRequest.class,
         new TestActionRequestHandler());

      final Settings serverSettings = Settings.builder()
         .put(TransportServer.SERVER_HOST, "0.0.0.0")
         .put(TransportServer.SERVER_PORT, 9090)
         .put(NodeService.NODE_CLASS, PlantNode.class.getName())
         .build();
      final TransportServer server = new TransportServer();

      final Settings clientSettings = Settings.builder()
         .put(TransportServer.SERVER_HOST, "0.0.0.0")
         .put(TransportServer.SERVER_PORT, 9090)
         .put(TransportServer.HEARTBEAT_PERIOD, 5000)
         .put("heartbeat.plantId", 15) // client heartbeat properties
         .build();
      final TransportClient client = new TransportClient();

      GlobalObservable.INSTANCE.addObserver(GlobalObservable.Types.CONNECTED,
         new Observer() {
            @Override
            public void update(Observable observable, Object arg) {
               System.out.println(NodeService.INSTANCE.nodes());
               Thread[] threads = new Thread[1];

               for(int i = 0; i < threads.length; i++) {
                  threads[i] = new ActionThread();
               }

               for(Thread thread : threads) {
                  thread.start();
               }

               System.out.println(observable.countObservers());
               observable.deleteObserver(this);
               System.out.println(observable.countObservers());
            }
         });

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

      new Thread() {
         @Override
         public void run() {
            try {
               client.start(clientSettings);
            }
            catch(Exception e) {
            }
         }
      }.start();
   }

   static class ActionThread extends Thread {
      @Override
      public void run() {
         Set<Node> nodes = NodeService.INSTANCE.nodes();

         for(Node node : nodes) {
            try {
               TransportService.INSTANCE.sendRequest(node,
                  TestAction.class.getName(),
                  new TestActionRequest(),
                  new TestActionResponseHandler());
            }
            catch(IOException e) {
               logger.log(Level.SEVERE, e.getMessage(), e);
            }
         }
      }
   }
}
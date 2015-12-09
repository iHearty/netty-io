package cn.togeek.netty;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import cn.togeek.netty.client.TransportClient;
import cn.togeek.netty.concurrent.ThreadPool;
import cn.togeek.netty.exception.SettingsException;
import cn.togeek.netty.handler.GlobalObservable;
import cn.togeek.netty.handler.TransportService;
import cn.togeek.netty.server.TransportServer;

import io.netty.channel.ChannelId;

public class NettyTest {
   private static final Logger logger = Logger
      .getLogger(NettyTest.class.getName());

   public static void main(String[] args) throws SettingsException {
      TransportService.INSTANCE.registerRequestHandler(
         TestAction.class.getName(),
         ThreadPool.Names.GENERIC,
         TestActionRequest.class,
         new TestActionRequestHandler());

      final Settings settings = Settings.builder()
         .put(TransportServer.SERVER_HOST, "0.0.0.0")
         .put(TransportServer.SERVER_PORT, 9090)
         .build();
      final TransportServer server = new TransportServer();
      final TransportClient client = new TransportClient();

      GlobalObservable.INSTANCE.addObserver(new Observer() {
         @Override
         public void update(Observable observable, Object arg) {
            if(arg instanceof ChannelId) {
               ChannelId channelId = (ChannelId) arg;
               Thread[] threads = new Thread[1];

               for(int i = 0; i < threads.length; i++) {
                  threads[i] = new ActionThread(channelId);
               }

               for(Thread thread : threads) {
                  thread.start();
               }
            }
         }
      });

      new Thread() {
         @Override
         public void run() {
            try {
               server.start(settings);
            }
            catch(Exception e) {
            }
         }
      }.start();

      new Thread() {
         @Override
         public void run() {
            try {
               client.start(settings);
            }
            catch(Exception e) {
            }
         }
      }.start();
   }

   static class ActionThread extends Thread {
      private ChannelId channelId;

      public ActionThread(ChannelId channelId) {
         this.channelId = channelId;
      }

      @Override
      public void run() {
         try {
            TransportService.INSTANCE.sendRequest(channelId,
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
package cn.togeek.netty;

import java.util.Observable;
import java.util.Observer;

import cn.togeek.netty.client.TransportClient;
import cn.togeek.netty.handler.GlobalObservable;
import cn.togeek.netty.handler.TransportService;
import cn.togeek.netty.server.TransportServer;

import io.netty.channel.ChannelId;

public class NettyTest {
   public static void main(String[] args) throws SettingsException {
      TransportService.registerRequestHandler(TestAction.class.getName(),
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
               TransportService.sendRequest(channelId,
                  TestAction.class.getName(),
                  "hello".getBytes(),
                  new TestActionResponseHandler());
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
}
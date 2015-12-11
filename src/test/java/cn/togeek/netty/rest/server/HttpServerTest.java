package cn.togeek.netty.rest.server;

import org.restlet.Component;
import org.restlet.data.Protocol;

public class HttpServerTest {
   public static void main(String[] args) throws Exception {
      Component component = new Component();
      component.getServers().add(Protocol.HTTP, 8080);
      component.getServices().add(new HttpTransportService());
      component.start();
   }
}
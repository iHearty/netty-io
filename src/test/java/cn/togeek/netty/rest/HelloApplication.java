package cn.togeek.netty.rest;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class HelloApplication extends Application {
   @Override
   public Restlet getInboundRoot() {
      Router router = new Router();
      router.attach("/hello", HelloResource.class);

      return router;
   }
}
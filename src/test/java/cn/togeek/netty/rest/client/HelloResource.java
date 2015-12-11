package cn.togeek.netty.rest.client;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class HelloResource extends ServerResource {
   @Get
   public String greet() {
      return "client see hello";
   }
}
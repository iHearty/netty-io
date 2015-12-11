package cn.togeek.netty.rest;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class HelloResource extends ServerResource {
   @Get
   public String greet() {
      return "hello http";
   }
}
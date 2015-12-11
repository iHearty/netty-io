package cn.togeek.netty.rest.client;

import java.util.HashMap;
import java.util.Map;

import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class HelloResource extends ServerResource {
   @Get
   public Representation greet() {
      Map<String, String> map = new HashMap<>();
      map.put("client", "hello");
      return new JacksonRepresentation<>(map);
   }

   @Post
   public Representation say(Map map) {
      map.put("feedback", "xxx");
      return new JacksonRepresentation<>(map);
   }
}
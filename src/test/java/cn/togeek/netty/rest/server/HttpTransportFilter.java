package cn.togeek.netty.rest.server;

import java.util.Set;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Filter;

import cn.togeek.netty.handler.Node;
import cn.togeek.netty.handler.NodeService;
import cn.togeek.netty.rest.HttpTransportAction;

public class HttpTransportFilter extends Filter {
   @Override
   protected int beforeHandle(Request request, Response response) {
      System.out.println(" before handle --- ");
      return super.beforeHandle(request, response);
   }

   @Override
   protected int doHandle(Request request, Response response) {
      System.out.println(" do handle --- ");

      Set<Node> nodes = NodeService.INSTANCE.nodes();

      for(Node node : nodes) {
         HttpTransportAction action = new HttpTransportAction();
         action.execute(node, request, response);
      }

      return super.doHandle(request, response);
   }

   @Override
   protected void afterHandle(Request request, Response response) {
      System.out.println(" after handle --- ");
      super.afterHandle(request, response);
   }
}
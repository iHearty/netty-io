package cn.togeek.netty.rest.server;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Filter;

import cn.togeek.netty.handler.Node;
import cn.togeek.netty.handler.NodeService;
import cn.togeek.netty.rest.HttpTransportAction;

public class HttpTransportFilter extends Filter {
   @Override
   protected int beforeHandle(Request request, Response response) {
      return super.beforeHandle(request, response);
   }

   @Override
   protected int doHandle(Request request, Response response) {
      Node node = NodeService.INSTANCE.find(15);
      new HttpTransportAction().execute(node, request, response);
      return CONTINUE;
   }

   @Override
   protected void afterHandle(Request request, Response response) {
      super.afterHandle(request, response);
   }
}
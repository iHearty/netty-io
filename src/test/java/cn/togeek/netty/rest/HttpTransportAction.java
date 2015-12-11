package cn.togeek.netty.rest;

import java.io.IOException;

import org.restlet.Request;
import org.restlet.Response;

import cn.togeek.netty.action.Action;
import cn.togeek.netty.concurrent.ThreadPool;
import cn.togeek.netty.exception.TransportException;
import cn.togeek.netty.handler.Node;
import cn.togeek.netty.handler.TransportChannel;
import cn.togeek.netty.handler.TransportRequest;
import cn.togeek.netty.handler.TransportRequestHandler;
import cn.togeek.netty.handler.TransportResponse;
import cn.togeek.netty.handler.TransportResponseHandler;
import cn.togeek.netty.handler.TransportService;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class HttpTransportAction implements Action {
   public HttpTransportAction() {
      TransportService.INSTANCE.registerRequestHandler(
         HttpTransportAction.class.getName(),
         ThreadPool.Names.GENERIC,
         new HttpTransportRequest(),
         new HttpTransportRequestHandler());
   }

   public void execute(Node node, Request request, Response response) {
      try {
         TransportService.INSTANCE.sendRequest(node,
            HttpTransportAction.class.getName(),
            new HttpTransportRequest(),
            new HttpTransportResponseHandler());
      }
      catch(IOException e) {
         e.printStackTrace();
      }
   }

   public class HttpTransportRequest implements TransportRequest {
      @Override
      public void readFrom(ByteBuf in) throws IOException {
         System.out
            .println(" HttpTransportRequest read == " + new String(in.array()));
      }

      @Override
      public ByteBuf writeTo() throws IOException {
         System.out.println(" HttpTransportRequest write == ");
         return Unpooled
            .copiedBuffer("HttpTransportRequest see hello".getBytes());
      }
   }

   public class HttpTransportRequestHandler
      implements TransportRequestHandler<HttpTransportRequest>
   {
      @Override
      public void handle(HttpTransportRequest request, TransportChannel channel)
         throws Exception
      {
         System.out
            .println(" HttpTransportRequestHandler == handle request ");
         channel.sendResponse(new HttpTransportResponse());
      }
   }

   class HttpTransportResponseHandler
      implements TransportResponseHandler<HttpTransportResponse>
   {
      @Override
      public HttpTransportResponse newInstance() {
         return new HttpTransportResponse();
      }

      @Override
      public void handleResponse(HttpTransportResponse response) {
         System.out
            .println(" HttpTransportResponseHandler == handle response ");
      }

      @Override
      public void handleException(TransportException exception) {
         System.out
            .println(" HttpTransportResponseHandler == handle exception ");
      }

      @Override
      public String executor() {
         return ThreadPool.Names.SAME;
      }
   }

   class HttpTransportResponse implements TransportResponse {
      @Override
      public void readFrom(ByteBuf in) throws IOException {
         System.out.println(
            " HttpTransportResponse read == " + new String(in.array()));
      }

      @Override
      public ByteBuf writeTo() throws IOException {
         System.out.println(" HttpTransportResponse write == ");
         return Unpooled
            .copiedBuffer("HttpTransportResponse see hi".getBytes());
      }
   }
}
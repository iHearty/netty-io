package cn.togeek.netty.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.fasterxml.jackson.databind.JsonNode;

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
import cn.togeek.netty.util.ByteBufs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class HttpTransportAction implements Action {
   private static final Logger logger = Logger
      .getLogger(HttpTransportAction.class.getName());

   private CountDownLatch latch = new CountDownLatch(1);

   public HttpTransportAction() {
      TransportService.INSTANCE.registerRequestHandler(
         HttpTransportAction.class.getName(),
         ThreadPool.Names.GENERIC,
         new HttpTransportRequest(),
         new HttpTransportRequestHandler());
   }

   public void execute(Node node, Request request, Response response) {
      Reference ref = request.getResourceRef().clone();
      ref.setScheme("http");
      ref.setHostPort(52500);
      ref.setHostDomain("127.0.0.1");

      Form form = request.getResourceRef().getQueryAsForm();

      if(form != null && !form.isEmpty()) {
         ref.setQuery(form.getQueryString());
      }

      String uri = ref.toString();
      String method = request.getMethod().getName();
      Representation entity = request.getEntity();
      HttpTransportRequest req =
         new HttpTransportRequest(uri, method, entity);

      try {
         TransportService.INSTANCE.sendRequest(node,
            HttpTransportAction.class.getName(),
            req,
            new HttpTransportResponseHandler(response));
      }
      catch(IOException e) {
         logger.log(Level.WARNING, "failed to send request to[" + node + "]",
            e);
      }

      try {
         latch.await();
      }
      catch(InterruptedException e) {
         latch.countDown();
      }
   }

   private void writeEntity(Representation entity, ByteBuf out)
      throws IOException
   {
      out.writeBoolean(entity == null || entity.isEmpty() ? true : false);

      if(entity != null && !entity.isEmpty()) {
         try(ByteArrayOutputStream bao = new ByteArrayOutputStream()) {
            entity.write(bao);
            byte[] bytes = bao.toByteArray();
            out.writeInt(bytes.length);
            out.writeBytes(bytes);
         }
      }
   }

   private Representation readEntity(ByteBuf in) {
      boolean empty = in.readBoolean();

      if(!empty) {
         int length = in.readInt();
         byte[] bytes = new byte[length];
         in.readBytes(bytes);
         return new ByteArrayRepresentation(bytes);
      }

      return null;
   }

   public class HttpTransportRequest implements TransportRequest {
      private String uri;

      private String method;

      private Representation entity;

      public HttpTransportRequest() {
      }

      public HttpTransportRequest(String uri,
                                  String method,
                                  Representation entity)
      {
         this.uri = uri;
         this.method = method;
         this.entity = entity;
      }

      @Override
      public void readFrom(ByteBuf in) throws IOException {
         uri = ByteBufs.readString(in);
         method = ByteBufs.readString(in);
         entity = readEntity(in);
      }

      @Override
      public ByteBuf writeTo() throws IOException {
         ByteBuf out = Unpooled.buffer();
         ByteBufs.writeString(uri, out);
         ByteBufs.writeString(method, out);
         writeEntity(entity, out);
         return out;
      }

      public String getUri() {
         return uri;
      }

      public String getMethod() {
         return method;
      }

      public Representation getEntity() {
         return entity;
      }
   }

   public class HttpTransportRequestHandler
      implements TransportRequestHandler<HttpTransportRequest>
   {
      @Override
      public void handle(HttpTransportRequest request, TransportChannel channel)
         throws Exception
      {
         final Context context = new Context();
         context.getParameters().add("readTimeout", "180000");
         ClientResource resource =
            new ClientResource(context, request.getUri());
         String method = request.getMethod();
         Representation entity = request.getEntity();

         Representation representation = null;

         if(Method.GET.getName().equals(method)) {
            representation = resource.get();
         }
         else if(Method.POST.getName().equals(method)) {
            entity = new JacksonRepresentation<>(entity, JsonNode.class);
            representation = resource.post(entity, MediaType.APPLICATION_JSON);
         }
         else if(Method.PUT.getName().equals(method)) {
            entity = new JacksonRepresentation<>(entity, JsonNode.class);
            representation = resource.put(entity, MediaType.APPLICATION_JSON);
         }
         else if(Method.DELETE.getName().equals(method)) {
            representation = resource.delete();
         }

         channel.sendResponse(new HttpTransportResponse(representation));
      }
   }

   class HttpTransportResponseHandler
      implements TransportResponseHandler<HttpTransportResponse>
   {
      private Response httpRes;

      public HttpTransportResponseHandler(Response httpRes) {
         this.httpRes = httpRes;
      }

      @Override
      public HttpTransportResponse newInstance() {
         return new HttpTransportResponse();
      }

      @Override
      public void handleResponse(HttpTransportResponse response) {
         httpRes.setEntity(response.getEntity());
         latch.countDown();
      }

      @Override
      public void handleException(TransportException exception) {
         logger.log(Level.WARNING, exception.getMessage(), exception);
      }

      @Override
      public String executor() {
         return ThreadPool.Names.SAME;
      }
   }

   class HttpTransportResponse implements TransportResponse {
      private Representation entity;

      public HttpTransportResponse() {
      }

      public HttpTransportResponse(Representation entity) {
         this.entity = entity;
      }

      public Representation getEntity() {
         return entity;
      }

      @Override
      public void readFrom(ByteBuf in) throws IOException {
         entity = readEntity(in);
      }

      @Override
      public ByteBuf writeTo() throws IOException {
         ByteBuf out = Unpooled.buffer();
         writeEntity(entity, out);
         return out;
      }
   }
}
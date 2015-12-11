package cn.togeek.netty.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

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
   public HttpTransportAction() {
      TransportService.INSTANCE.registerRequestHandler(
         HttpTransportAction.class.getName(),
         ThreadPool.Names.GENERIC,
         new HttpTransportRequest(),
         new HttpTransportRequestHandler());
   }

   public void execute(Node node, Request request, Response response) {
      try {
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

         TransportService.INSTANCE.sendRequest(node,
            HttpTransportAction.class.getName(),
            req,
            new HttpTransportResponseHandler());
      }
      catch(IOException e) {
         e.printStackTrace();
      }
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
         System.out
            .println(" HttpTransportRequest read == " + new String(in.array()));
         uri = ByteBufs.readString(in);
         method = ByteBufs.readString(in);

         boolean empty = in.readBoolean();

         if(!empty) {
            int length = in.readInt();
            byte[] bytes = new byte[length];
            in.readBytes(bytes);
            entity = new ByteArrayRepresentation(bytes);
         }
      }

      @Override
      public ByteBuf writeTo() throws IOException {
         System.out.println(" HttpTransportRequest write == ");
         ByteBuf out = Unpooled.buffer();
         ByteBufs.writeString(uri, out);
         ByteBufs.writeString(method, out);

         // write entity
         out.writeBoolean(entity == null || entity.isEmpty() ? true : false);

         if(entity != null && !entity.isEmpty()) {
            try(ByteArrayOutputStream bao = new ByteArrayOutputStream()) {
               entity.write(bao);
               byte[] bytes = bao.toByteArray();
               out.writeInt(bytes.length);
               out.writeBytes(bytes);
            }
         }

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
         System.out
            .println(" HttpTransportRequestHandler == handle request ");

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
            representation = resource.post(entity, MediaType.APPLICATION_JSON);
         }
         else if(Method.PUT.getName().equals(method)) {
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
      @Override
      public HttpTransportResponse newInstance() {
         return new HttpTransportResponse();
      }

      @Override
      public void handleResponse(HttpTransportResponse response) {
         System.out
            .println(
               " HttpTransportResponseHandler == handle response " + response);
      }

      @Override
      public void handleException(TransportException exception) {
         System.out.println(" HttpTransportResponseHandler == handle exception "
            + exception);
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
         System.out.println(
            " HttpTransportResponse read == " + new String(in.array()));

         boolean empty = in.readBoolean();

         if(!empty) {
            int length = in.readInt();
            byte[] bytes = new byte[length];
            in.readBytes(bytes);
            entity = new ByteArrayRepresentation(bytes);
         }
      }

      @Override
      public ByteBuf writeTo() throws IOException {
         System.out.println(" HttpTransportResponse write == ");

         ByteBuf out = Unpooled.buffer();
         // write entity
         out.writeBoolean(entity == null || entity.isEmpty() ? true : false);

         if(entity != null && !entity.isEmpty()) {
            try(ByteArrayOutputStream bao = new ByteArrayOutputStream()) {
               entity.write(bao);
               byte[] bytes = bao.toByteArray();
               out.writeInt(bytes.length);
               out.writeBytes(bytes);
            }
         }

         return out;
      }
   }
}
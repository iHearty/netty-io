package cn.togeek.netty.handler;

public interface TransportResponseHandler<Response extends TransportResponse> {
   Response newInstance();

   void handleResponse(Response response);
}
package cn.togeek.netty.handler;

import cn.togeek.netty.exception.TransportException;

public interface TransportResponseHandler<Response extends TransportResponse> {
   Response newInstance();

   void handleResponse(Response response);

   void handleException(TransportException exception);
}
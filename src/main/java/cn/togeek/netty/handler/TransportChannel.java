package cn.togeek.netty.handler;

import java.io.IOException;

public interface TransportChannel {
   String action();

   void sendResponse(TransportResponse response) throws IOException;
}
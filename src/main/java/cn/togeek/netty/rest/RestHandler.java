package cn.togeek.netty.rest;

import org.restlet.Request;
import org.restlet.Response;

public interface RestHandler {
   void handleRequest(Request request, Response response) throws Exception;
}
package cn.togeek.netty;

import cn.togeek.netty.handler.TransportException;
import cn.togeek.netty.handler.TransportResponseHandler;

public class TestActionResponseHandler
   implements TransportResponseHandler<TestActionResponse>
{
   @Override
   public TestActionResponse newInstance() {
      return new TestActionResponse();
   }

   @Override
   public void handleResponse(TestActionResponse response) {
      System.out.println(" response handle ");
   }

   @Override
   public void handleException(TransportException exception) {

   }
}
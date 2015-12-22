package cn.togeek.netty;

import cn.togeek.netty.handler.TransportChannel;
import cn.togeek.netty.handler.TransportRequestHandler;

public class TestActionRequestHandler
   implements TransportRequestHandler<TestActionRequest>
{
   @Override
   public TestActionRequest newInstance() {
      // TODO Auto-generated method stub
      return new TestActionRequest();
   }

   @Override
   public void handle(TestActionRequest request, TransportChannel channel)
      throws Exception
   {
      channel.sendResponse(new TestActionResponse());
   }

}
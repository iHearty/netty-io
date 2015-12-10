package cn.togeek.netty.exception;

import cn.togeek.netty.handler.Node;

public class ReceiveTimeoutTransportException extends ActionTransportException {
   private static final long serialVersionUID = 1L;

   public ReceiveTimeoutTransportException(Node node,
                                           String action,
                                           String msg)
   {
      super(node.name(), action, msg, null);
   }
}
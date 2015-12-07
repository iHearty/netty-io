package cn.togeek.netty.exception;

public class ResponseHandlerFailureTransportException
   extends TransportException
{
   private static final long serialVersionUID = 1L;

   public ResponseHandlerFailureTransportException(Throwable cause) {
      super(cause);
   }
}
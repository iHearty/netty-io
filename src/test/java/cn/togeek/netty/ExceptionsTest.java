package cn.togeek.netty;

import java.io.IOException;

import cn.togeek.netty.exception.Exceptions;
import cn.togeek.netty.exception.SendRequestTransportException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;

public class ExceptionsTest {
   public static void main(String[] args) throws IOException {
      ByteBuf buffer = Unpooled.buffer();

      try {
         Throwable write = new NullPointerException("NullPointerException");
         Exceptions.writeThrowable(write, buffer);
         Throwable read = Exceptions.readThrowable(buffer);
         printThrowable(read);

         buffer.clear();

         write = new SendRequestTransportException(TestAction.class.getName(),
            write);
         Exceptions.writeThrowable(write, buffer);
         read = Exceptions.readThrowable(buffer);
         printThrowable(read);
      }
      finally {
         ReferenceCountUtil.release(buffer);
      }
   }

   private static void printThrowable(Throwable throwable) {
      StringBuffer buffer = new StringBuffer();
      buffer.append("Class:");
      buffer.append(throwable.getClass().getName());
      buffer.append("\n");
      buffer.append("Message:");
      buffer.append(throwable.getMessage());
      buffer.append("\n");
      buffer.append("Cause:");
      buffer.append(throwable.getCause());
      buffer.append("\n");

      StackTraceElement[] stackTraces = throwable.getStackTrace();

      for(StackTraceElement stack : stackTraces) {
         buffer.append(stack.toString());
      }

      buffer.append("\n\n");

      System.out.println(buffer.toString());
   }
}
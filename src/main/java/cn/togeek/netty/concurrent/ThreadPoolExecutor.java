package cn.togeek.netty.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutor
   extends java.util.concurrent.ThreadPoolExecutor
{
   /**
    * Name used in error reporting.
    */
   private final String name;

   ThreadPoolExecutor(String name,
                      int corePoolSize,
                      int maximumPoolSize,
                      long keepAliveTime,
                      TimeUnit unit,
                      BlockingQueue<Runnable> workQueue,
                      ThreadFactory threadFactory)
   {
      this(name, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
         threadFactory, new AbortPolicy());
   }

   ThreadPoolExecutor(String name,
                      int corePoolSize,
                      int maximumPoolSize,
                      long keepAliveTime,
                      TimeUnit unit,
                      BlockingQueue<Runnable> workQueue,
                      ThreadFactory threadFactory,
                      RejectedExecutionHandler handler)
   {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
         threadFactory, handler);

      this.name = name;
   }
}
package cn.togeek.netty.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import cn.togeek.netty.Settings;
import cn.togeek.netty.exception.SettingsException;

public class Executors {
   /**
    * Settings key to manually set the number of available processors.
    * This is used to adjust thread pools sizes etc. per node.
    */
   public static final String PROCESSORS = "processors";

   /** Useful for testing */
   public static final String DEFAULT_SYSPROP = "processors.override";

   /**
    * Returns the number of processors available but at most <tt>32</tt>.
    * 
    * @throws SettingsException
    */
   public static int boundedNumberOfProcessors(Settings settings) {
      /*
       * This relates to issues where machines with large number of cores
       * ie. >= 48 create too many threads and run into OOM see #3478
       * We just use an 32 core upper-bound here to not stress the system
       * too much with too many created threads
       */
      int defaultValue =
         Math.min(32, Runtime.getRuntime().availableProcessors());

      try {
         defaultValue = Integer.parseInt(System.getProperty(DEFAULT_SYSPROP));
         return settings.getAsInt(PROCESSORS, defaultValue);
      }
      catch(Throwable ignored) {
      }

      return defaultValue;
   }

   public static ThreadPoolExecutor newCached(String name,
                                              long keepAliveTime,
                                              TimeUnit unit,
                                              java.util.concurrent.ThreadFactory threadFactory)
   {
      return new ThreadPoolExecutor(name, 0, Integer.MAX_VALUE, keepAliveTime,
         unit, new SynchronousQueue<Runnable>(), threadFactory,
         new DiscardOldestPolicy());
   }

   public static ThreadPoolExecutor newFixed(String name,
                                             int size,
                                             int queueCapacity,
                                             java.util.concurrent.ThreadFactory threadFactory)
   {
      BlockingQueue<Runnable> queue;

      if(queueCapacity < 0) {
         queue = new LinkedTransferQueue<>();
      }
      else {
         queue = new SizeBlockingQueue<>(
            new LinkedTransferQueue<Runnable>(), queueCapacity);
      }

      return new ThreadPoolExecutor(name, size, size, 0,
         TimeUnit.MILLISECONDS, queue, threadFactory,
         new DiscardOldestPolicy());
   }

   public static ThreadPoolExecutor newScaling(String name,
                                               int min,
                                               int max,
                                               long keepAliveTime,
                                               TimeUnit unit,
                                               java.util.concurrent.ThreadFactory threadFactory)
   {
      ExecutorScalingQueue<Runnable> queue = new ExecutorScalingQueue<>();
      // we force the execution, since we might run into concurrency issues in
      // offer for ScalingBlockingQueue
      ThreadPoolExecutor executor = new ThreadPoolExecutor(name, min, max,
         keepAliveTime, unit, queue, threadFactory, new ForceQueuePolicy());
      queue.executor = executor;
      return executor;
   }

   public static String threadName(Settings settings, String namePrefix) {
      String name = settings.get("name");

      if(name == null) {
         name = "netty";
      }
      else {
         name = "netty[" + name + "]";
      }

      return name + "[" + namePrefix + "]";
   }

   public static ThreadFactory daemonThreadFactory(Settings settings,
                                                   String namePrefix)
   {
      return daemonThreadFactory(threadName(settings, namePrefix));
   }

   public static ThreadFactory daemonThreadFactory(String namePrefix) {
      return new ThreadFactory(namePrefix);
   }

   static class ThreadFactory implements java.util.concurrent.ThreadFactory {
      final ThreadGroup group;

      final AtomicInteger threadNumber = new AtomicInteger(1);

      final String namePrefix;

      public ThreadFactory(String namePrefix) {
         this.namePrefix = namePrefix;
         SecurityManager s = System.getSecurityManager();
         group = (s != null) ? s.getThreadGroup()
            : Thread.currentThread().getThreadGroup();
      }

      @Override
      public Thread newThread(Runnable r) {
         Thread t = new Thread(group, r,
            namePrefix + "[T#" + threadNumber.getAndIncrement() + "]",
            0);
         t.setDaemon(true);
         return t;
      }
   }

   static class ExecutorScalingQueue<E> extends LinkedTransferQueue<E> {
      private static final long serialVersionUID = 1L;

      ThreadPoolExecutor executor;

      public ExecutorScalingQueue() {
      }

      @Override
      public boolean offer(E e) {
         if(!tryTransfer(e)) {
            int left =
               executor.getMaximumPoolSize() - executor.getCorePoolSize();

            if(left > 0) {
               return false;
            }
            else {
               return super.offer(e);
            }
         }
         else {
            return true;
         }
      }
   }

   /**
    * A handler for rejected tasks that adds the specified element to this
    * queue, waiting if necessary for space to become available.
    */
   static class ForceQueuePolicy implements RejectedExecutionHandler {
      @Override
      public void
         rejectedExecution(Runnable r,
                           java.util.concurrent.ThreadPoolExecutor executor)
      {
         try {
            executor.getQueue().put(r);
         }
         catch(InterruptedException e) {
            // should never happen since we never wait
         }
      }
   }

   private Executors() {
   }
}